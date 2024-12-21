package kurd.reco.roxio.ui.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AuthScreenDestination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.AuthVM
import kurd.reco.core.MainVM
import kurd.reco.core.api.Resource
import kurd.reco.core.copyText
import kurd.reco.roxio.common.CircularProgressIndicator
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
            }

            Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Success -> {
                mainVM.accessToken = resource.value
                navigator.navigate(HomeScreenDestination)
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

    val tfFocusRequester = remember { FocusRequester() }
    val tfFocusRequester2 = remember { FocusRequester() }

    val sharedPreferences = context.getSharedPreferences("roxio_auth", Context.MODE_PRIVATE)

    val androidID = viewModel.getAndroidID(context)

    val loginStatus by viewModel.loginState.state.collectAsStateWithLifecycle()
    val accessToken by viewModel.accessToken.state.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    when (val resource = loginStatus) {
        is Resource.Failure -> {
            Toast.makeText(context, "Error: ${resource.error}", Toast.LENGTH_SHORT).show()
            if (resource.error.contains("hwid limit")) {
                copyText(androidID, "HWID copied / $androidID",context)
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
                    Toast.makeText(context, "Error: ${token.error}", Toast.LENGTH_LONG).show()
                    navigator.navigate(AuthScreenDestination)
                }
                Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    mainVM.accessToken = token.value
                    navigator.navigate(HomeScreenDestination)
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

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            shape = ClickableSurfaceDefaults.shape(shape = ShapeDefaults.ExtraSmall),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                focusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                pressedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                focusedContentColor = MaterialTheme.colorScheme.onSurface,
                pressedContentColor = MaterialTheme.colorScheme.onSurface
            ),
            tonalElevation = 2.dp,
            onClick = { tfFocusRequester.requestFocus() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .padding(start = 20.dp),
            ) {
                BasicTextField(
                    modifier = Modifier
                        .focusRequester(tfFocusRequester),
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (username.isEmpty()) {
                    Text(text = "Username", style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            shape = ClickableSurfaceDefaults.shape(shape = ShapeDefaults.ExtraSmall),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                focusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                pressedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                focusedContentColor = MaterialTheme.colorScheme.onSurface,
                pressedContentColor = MaterialTheme.colorScheme.onSurface
            ),
            tonalElevation = 2.dp,
            onClick = { tfFocusRequester2.requestFocus() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .padding(start = 20.dp),
            ) {
                BasicTextField(
                    modifier = Modifier
                        .focusRequester(tfFocusRequester2),
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (password.isEmpty())
                    Text(text = "Password", style = MaterialTheme.typography.titleLarge)
            }
        }

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

