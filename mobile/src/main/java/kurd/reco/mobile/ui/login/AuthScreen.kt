package kurd.reco.mobile.ui.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AuthScreenDestination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.viewmodels.AuthVM
import kurd.reco.core.Global
import kurd.reco.core.User
import kurd.reco.core.api.Resource
import kurd.reco.core.copyText
import kurd.reco.mobile.R
import kurd.reco.mobile.common.ErrorShower
import kurd.reco.core.data.ErrorModel
import org.koin.androidx.compose.koinViewModel

@Destination<RootGraph>(start = true)
@Composable
fun AuthScreenRoot(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val authVM: AuthVM = koinViewModel()

    val sharedPreferences = context.getSharedPreferences("roxio_auth", Context.MODE_PRIVATE)
    val rememberTokenFromPrefs = sharedPreferences.getString("remember_token", null)

    var errorModel by remember { mutableStateOf(ErrorModel("", false)) }


    if (rememberTokenFromPrefs == null) {
        navigator.navigate(AuthScreenDestination)
    } else {
        LaunchedEffect(Unit) {
            authVM.getToken(rememberTokenFromPrefs, authVM.getAndroidID(context))
        }
        val accessToken by authVM.accessToken.state.collectAsStateWithLifecycle()

        when (val resource = accessToken) {
            is Resource.Failure -> {
                LaunchedEffect(resource) {
                    Toast.makeText(context, resource.error, Toast.LENGTH_SHORT).show()
                    errorModel = ErrorModel(resource.error, true)
                }
            }
            Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                User.accessToken = resource.value
                navigator.navigate(HomeScreenRootDestination)
            }
        }

        if (errorModel.isError) {
            ErrorShower(
                errorText = errorModel.errorText,
                onRetry = {
                    errorModel = errorModel.copy(isError = false)
                    authVM.getToken(rememberTokenFromPrefs, authVM.getAndroidID(context))
                },
                onDismiss = {
                    errorModel = errorModel.copy(isError = false)
                    navigator.navigate(AuthScreenDestination)
                }
            )
        }
    }

}

@Destination<RootGraph>
@Composable
fun AuthScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val viewModel: AuthVM = koinViewModel()

    val sharedPreferences = context.getSharedPreferences("roxio_auth", Context.MODE_PRIVATE)

    val loginStatus by viewModel.loginState.state.collectAsStateWithLifecycle()
    val accessToken by viewModel.accessToken.state.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorModel by remember { mutableStateOf(ErrorModel("", false)) }
    val androidID = remember { viewModel.getAndroidID(context) }

    if (errorModel.isError) {
        ErrorShower(
            errorText = errorModel.errorText,
            onRetry = {
                errorModel = errorModel.copy(isError = false)
            },
            onDismiss = { errorModel = errorModel.copy(isError = false) }
        )
    }

    when (val resource = loginStatus) {
        is Resource.Failure -> {
            Toast.makeText(context, "Error: ${resource.error}", Toast.LENGTH_SHORT).show()
            if (resource.error.contains("hwid limit")) {
                copyText(androidID, "HWID copied / $androidID",context)
            }
            isLoading = false
            errorModel = ErrorModel(resource.error, true)

            viewModel.resetLoginState()
        }
        is Resource.Success -> {
            val rememberToken = resource.value

            LaunchedEffect(Unit) {
                viewModel.getToken(rememberToken, androidID)
                sharedPreferences.edit().putString("remember_token", rememberToken).apply()
            }

            when (val token = accessToken) {
                is Resource.Failure -> {
                    LaunchedEffect(token) {
                        Toast.makeText(context, "Error: ${token.error}", Toast.LENGTH_SHORT).show()
                        errorModel = ErrorModel(token.error, true)
                    }
                    navigator.navigate(AuthScreenDestination)
                }
                Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    User.accessToken = token.value
                    navigator.navigate(HomeScreenRootDestination)
                }
            }
        }
        Resource.Loading -> {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(R.string.username)) },
            leadingIcon = {
                Icon(Icons.Default.Person, null)
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            leadingIcon = {
                Icon(Icons.Default.Lock, null)
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context,
                        context.getString(R.string.enter_username_and_password), Toast.LENGTH_SHORT).show()
                    return@Button
                } else if (Global.loginTryCount >= 6) {
                    Toast.makeText(context,
                        context.getString(R.string.too_many_attempts), Toast.LENGTH_SHORT).show()
                    return@Button
                } else {
                    Global.loginTryCount++

                    isLoading = true
                    viewModel.login(username, password, androidID)
                }
            },
        ) {
            Text(stringResource(R.string.login))
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}