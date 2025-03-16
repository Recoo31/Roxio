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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AuthScreenDestination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.viewmodels.AuthVM
import kurd.reco.core.Global
import kurd.reco.core.User
import kurd.reco.core.api.Resource
import kurd.reco.core.copyText
import kurd.reco.roxio.R
import kurd.reco.roxio.common.CircularProgressIndicator
import kurd.reco.roxio.common.CustomTextField
import org.koin.androidx.compose.koinViewModel

@Destination<RootGraph>(start = true)
@Composable
fun AuthScreenRoot(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val authVM: AuthVM = koinViewModel()

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
                User.accessToken = resource.value
                navigator.navigate(HomeScreenDestination)
            }
        }
    }

}

@Destination<RootGraph>
@Composable
fun AuthScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val viewModel: AuthVM = koinViewModel()

    val sharedPreferences = context.getSharedPreferences("roxio_auth", Context.MODE_PRIVATE)

    val loginStatus by viewModel.loginState.state.collectAsStateWithLifecycle()
    val accessToken by viewModel.accessToken.state.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val androidID = remember { viewModel.getAndroidID(context) }

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CustomTextField(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(16.dp),
            value = username,
            onValueChange = { username = it },
            placeholder = stringResource(id = R.string.username),
            leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.padding(end = 16.dp)) },
            placeholderStyle = MaterialTheme.typography.titleLarge,
            focusRequester = focusRequester,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        CustomTextField(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(16.dp),
            value = password,
            onValueChange = { password = it },
            placeholder = stringResource(id = R.string.password),
            leadingIcon = { Icon(Icons.Default.Lock, null, modifier = Modifier.padding(end = 16.dp)) },
            placeholderStyle = MaterialTheme.typography.titleLarge,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Button(
            onClick = {
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.enter_username_and_password), Toast.LENGTH_SHORT).show()
                    return@Button
                } else {
                    Global.loginTryCount++
                    isLoading = true

                    viewModel.resetLoginState()
                    viewModel.login(username, password, androidID)
                }
            },
        ) {
            Text(stringResource(id = R.string.login))
        }
    }

    when (val resource = loginStatus) {
        is Resource.Failure -> {
            isLoading = false
            Toast.makeText(context, "Error: ${resource.error}", Toast.LENGTH_SHORT).show()
            if (resource.error.contains("hwid limit")) {
                copyText(androidID, "HWID copied / $androidID",context)
            }
            viewModel.resetLoginState()
        }
        is Resource.Success -> {
            val rememberToken = resource.value
            isLoading = false

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
                    User.accessToken = token.value
                    navigator.navigate(HomeScreenDestination)
                }
            }
        }
        Resource.Loading -> {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

