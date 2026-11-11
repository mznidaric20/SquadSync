package hr.foi.squadsync

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    vm: LoginViewModel = viewModel(),
    onRegistrationClick: () -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // ZAGLAVLJE APLIKACIJE

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Mjesto za logotip

            Box(
                modifier = Modifier
                    .width(45.dp)
                    .height(36.dp)
                    .border(1.dp, Color.Gray),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "LOGO",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            // Naziv aplikacije

            Text(
                text = "SquadSync",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        HorizontalDivider(
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // NASLOV PRIJAVA

        Text(
            text = "Prijava",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(42.dp))

        // E-MAIL

        Text(
            text = "E-mail",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            value = vm.email,
            onValueChange = {
                vm.promijeniEmail(it)
            },

            modifier = Modifier.fillMaxWidth(),

            placeholder = {
                Text("ime@primjer.com")
            },

            singleLine = true,

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),

            isError = vm.greske.email != null,

            shape = RoundedCornerShape(2.dp)
        )

        // Pogreska e-maila

        vm.greske.email?.let { greska ->

            Text(
                text = greska,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // LOZINKA

        Text(
            text = "Lozinka",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            value = vm.lozinka,
            onValueChange = {
                vm.promijeniLozinku(it)
            },

            modifier = Modifier.fillMaxWidth(),

            placeholder = {
                Text("••••••••")
            },

            singleLine = true,

            visualTransformation =
                PasswordVisualTransformation(),

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),

            isError = vm.greske.lozinka != null,

            shape = RoundedCornerShape(2.dp)
        )

        // Pogreska lozinke

        vm.greske.lozinka?.let { greska ->

            Text(
                text = greska,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // GUMB PRIJAVI SE

        Button(
            onClick = {
                vm.provjeriPodatke()
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),

            shape = RoundedCornerShape(5.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray
            )
        ) {

            Text(
                text = "PRIJAVI SE",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // PORUKA NAKON PROVJERE

        if (vm.poruka.isNotEmpty()) {

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = vm.poruka,

                color = if (
                    vm.greske.email != null ||
                    vm.greske.lozinka != null
                ) {
                    MaterialTheme.colorScheme.error
                } else {
                    Color(0xFF0F5C4D)
                },

                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PRELAZAK NA REGISTRACIJU

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Nemate račun? ",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Registracija  ›",
                fontSize = 14.sp,
                color = Color.DarkGray,

                modifier = Modifier.clickable {
                    onRegistrationClick()
                }
            )
        }
    }
}


// PREVIEW EKRANA PRIJAVE

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {

    LoginScreen(
        vm = LoginViewModel()
    )
}