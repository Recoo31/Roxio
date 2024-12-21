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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AuthScreenDestination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.AuthVM
import kurd.reco.core.MainVM
import kurd.reco.core.api.Resource
import kurd.reco.core.copyText
import kurd.reco.mobile.common.ErrorShower
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Destination<RootGraph>(start = true)
@Composable
fun AuthScreenRoot(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val authVM: AuthVM = koinViewModel()
    val mainVM: MainVM = koinInject()

    val sharedPreferences = context.getSharedPreferences("roxio_auth", Context.MODE_PRIVATE)
    val rememberTokenFromPrefs = sharedPreferences.getString("remember_token", null)

    if (rememberTokenFromPrefs == null) {
        navigator.navigate(AuthScreenDestination)
    } else {
        LaunchedEffect(Unit) {
            authVM.getToken(rememberTokenFromPrefs, authVM.getAndroidID(context))
        }
        val accessToken by authVM.accessToken.state.collectAsStateWithLifecycle()

        when (val resource = accessToken) {
            is Resource.Failure -> {
                navigator.navigate(AuthScreenDestination)
                Toast.makeText(context, resource.error, Toast.LENGTH_SHORT).show()
            }
            Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                mainVM.accessToken = resource.value
                navigator.navigate(HomeScreenRootDestination)
            }
        }
    }

}

@Destination<RootGraph>
@Composable
fun AuthScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val viewModel: AuthVM = koinViewModel()
    val mainVM: MainVM = koinInject()

    val sharedPreferences = context.getSharedPreferences("roxio_auth", Context.MODE_PRIVATE)

    val androidID = viewModel.getAndroidID(context)
    val loginStatus by viewModel.loginState.state.collectAsStateWithLifecycle()

    val accessToken by viewModel.accessToken.state.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

//    LaunchedEffect(Unit) {
//        launch(Dispatchers.IO) {
//            println(appWithProxy.get(url = "https://pastebin.com").text)
//        }
//    }

    if (isError) {
        ErrorShower(
            errorText = errorText,
            onRetry = {
                isError = false
            },
            onDismiss = { isError = false }
        )
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    when (val resource = loginStatus) {
        is Resource.Failure -> {
            LaunchedEffect(resource) {
                Toast.makeText(context, "Error: ${resource.error}", Toast.LENGTH_SHORT).show()
                if (resource.error.contains("hwid limit")) {
                    copyText(androidID, "HWID copied / $androidID",context)
                }
                isError = true
                isLoading = false
                errorText = resource.error
            }
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
                        isError = true
                        errorText = token.error
                    }
                    navigator.navigate(AuthScreenDestination)
                }
                Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    mainVM.accessToken = token.value
                    navigator.navigate(HomeScreenRootDestination)
                }
            }
        }
        Resource.Loading -> {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                viewModel.login(username, password, androidID)
            },
        ) {
            Text("Login")
        }

    }
}