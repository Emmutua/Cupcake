/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.cupcake

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cupcake.data.DataSource.flavors
import com.example.cupcake.data.DataSource.quantityOptions
import com.example.cupcake.ui.OrderSummaryScreen
import com.example.cupcake.ui.OrderViewModel
import com.example.cupcake.ui.SelectOptionScreen
import com.example.cupcake.ui.StartOrderScreen

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */

//these are the routes - unique string for accessing each screen
enum class CupcakeScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Flavour(title = R.string.choose_flavor),
    Pickup(title = R.string.choose_pickup_date),
    Summary(title = R.string.order_summary)
}
sealed class CupcakeDestination(val title: String){
    object Start : CupcakeDestination("Start")
    object Flavour: CupcakeDestination("Flavour")
    object PickUp: CupcakeDestination("PickUp")
    object Summary: CupcakeDestination("Summary")

}


@Composable
fun CupcakeAppBar(
    currentScreen:CupcakeDestination,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = currentScreen.title) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun CupcakeApp(modifier: Modifier = Modifier, viewModel: OrderViewModel = viewModel()) {
    // TODO: Create NavController
    val navController = rememberNavController()
    // TODO: Get current back stack entry
val backStackEntry by navController.currentBackStackEntryAsState()
    // TODO: Get the name of the current screen
val currentScreen = CupcakeScreen.valueOf(
    backStackEntry?.destination?.route ?: CupcakeScreen.Start.name
)
    Scaffold(
        topBar = {
            CupcakeAppBar(
                currentScreen = CupcakeDestination.Start,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        // TODO: add NavHost
        NavHost(
            navController = navController,
            startDestination = CupcakeDestination.Start.title,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(
                route = CupcakeDestination.Start.title
            ) {
                StartOrderScreen(quantityOptions = quantityOptions, onNextButtonClicked = {
                    navController.navigate(CupcakeDestination.PickUp.title)
                    viewModel.setQuantity(it)
                })
            }
            composable(
                route = CupcakeDestination.PickUp.title
            ) {
                val context =
                    LocalContext.current //You can use this variable to get the strings from the list of resource IDs
                SelectOptionScreen(subtotal = uiState.price,
                    onCancelButtonClicked = {
                                            cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    onNextButtonClicked = {
                        navController.navigate(CupcakeDestination.PickUp.title)
                    },
                    options = flavors.map {
                        context.resources.getString(id)
                    },
                    onSelectionChanged = { viewModel.setFlavor(it) })
            }
            composable(
                route = CupcakeDestination.PickUp.title
            ) {
                SelectOptionScreen(
                    subtotal = uiState.price,
                    onNextButtonClicked = {
                        navController.navigate(CupcakeDestination.Summary.title)
                    },
                    onCancelButtonClicked = { cancelOrderAndNavigateToStart(viewModel, navController) },
                    options = uiState.pickupOptions,
                    onSelectionChanged = { viewModel.setDate(it) })
            }
            composable(
                route = CupcakeDestination.Summary.title
            ) {
                OrderSummaryScreen(orderUiState = uiState, onCancelButtonClicked = { cancelOrderAndNavigateToStart(viewModel,navController) })
            }
        }
    }
}

private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(CupcakeScreen.Start.name, inclusive = false)
}