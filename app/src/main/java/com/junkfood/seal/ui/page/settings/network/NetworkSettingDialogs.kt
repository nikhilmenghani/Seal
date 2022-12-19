package com.junkfood.seal.ui.page.settings.network

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.PasteButton
import com.junkfood.seal.ui.component.TextButtonWithIcon
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.COOKIES_DOMAIN
import com.junkfood.seal.util.TextUtil.isNumberInRange
import com.junkfood.seal.util.TextUtil.matchUrlFromClipboard
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateLimitDialog(onDismissRequest: () -> Unit) {
    var isError by remember { mutableStateOf(false) }
    var maxRate by remember {
        mutableStateOf(PreferenceUtil.getMaxDownloadRate())
    }
    AlertDialog(onDismissRequest = onDismissRequest, icon = {
        Icon(Icons.Outlined.Speed, null)
    }, title = { Text(stringResource(R.string.rate_limit)) }, text = {
        Column {
            Text(
                stringResource(R.string.rate_limit_desc),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                isError = isError,
                supportingText = {
                    Text(
                        text = if (isError) stringResource(R.string.invalid_input) else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                value = maxRate,
                label = { Text(stringResource(R.string.max_rate)) },
                onValueChange = {
                    if (it.isDigitsOnly()) maxRate = it
                    isError = false
                }, trailingIcon = { Text("K") })
        }
    }, dismissButton = {
        DismissButton {
            onDismissRequest()
        }
    }, confirmButton = {
        ConfirmButton {
            if (maxRate.isNumberInRange(1, 100_0000)) {
                PreferenceUtil.updateString(PreferenceUtil.MAX_RATE, maxRate)
                onDismissRequest()
            } else {
                isError = true
            }
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookiesDialog(
    navigateToCookieGeneratorPage: () -> Unit = {}, onDismissRequest: () -> Unit
) {
    var cookies by remember {
        mutableStateOf(PreferenceUtil.getCookies())
    }
    var url by remember { mutableStateOf(PreferenceUtil.getString(COOKIES_DOMAIN, "")) }
    AlertDialog(onDismissRequest = onDismissRequest, icon = {
        Icon(Icons.Outlined.Cookie, null)
    }, title = { Text(stringResource(R.string.cookies)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Text(
                stringResource(R.string.cookies_desc),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                value = url, label = { Text("URL") },
                onValueChange = { url = it }, trailingIcon = {
                    PasteButton { url = matchUrlFromClipboard(it) }
                }, maxLines = 1
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                value = cookies,
                label = { Text(stringResource(R.string.cookies_file_name)) },
                onValueChange = { cookies = it }, minLines = 8, maxLines = 8
            )
            TextButtonWithIcon(
                onClick = {
                    PreferenceUtil.updateString(COOKIES_DOMAIN, url)
                    navigateToCookieGeneratorPage()
                },
                icon = Icons.Outlined.GeneratingTokens,
                text = stringResource(id = R.string.generate_new_cookies)
            )

        }
    }, dismissButton = {
        DismissButton {
            onDismissRequest()
        }
    }, confirmButton = {
        ConfirmButton {
            onDismissRequest()
            PreferenceUtil.updateString(PreferenceUtil.COOKIES_FILE, cookies)
            PreferenceUtil.updateString(COOKIES_DOMAIN, url)
        }
    })
}

@Composable
fun ConcurrentDownloadDialog(
    onDismissRequest: () -> Unit,
) {
    var concurrentFragments by remember { mutableStateOf(PreferenceUtil.getConcurrentFragments()) }
    val count by remember {
        derivedStateOf {
            if (concurrentFragments <= 0.125f) 1 else ((concurrentFragments * 4f).roundToInt()) * 4
        }
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                PreferenceUtil.updateInt(PreferenceUtil.CONCURRENT, count)
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        icon = { Icon(Icons.Outlined.OfflineBolt, null) },
        title = { Text(stringResource(R.string.concurrent_download)) },
        text = {
            Column {
                Text(text = stringResource(R.string.concurrent_download_num, count))
                Slider(
                    value = concurrentFragments,
                    onValueChange = { concurrentFragments = it },
                    steps = 3,
                    valueRange = 0f..1f
                )
            }
        })
}