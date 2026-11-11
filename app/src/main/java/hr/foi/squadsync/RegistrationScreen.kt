package hr.foi.squadsync

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    vm: RegistrationViewModel = viewModel()
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ZAGLAVLJE APLIKACIJE

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

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

            Text(
                text = "SquadSync",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }

        HorizontalDivider()

        Text(
            text = "Registracija",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // IME I PREZIME

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(modifier = Modifier.weight(1f)) {

                RegistrationField(
                    label = "Ime *",
                    value = vm.ime,
                    onValueChange = { vm.ime = it },
                    error = vm.greske.ime
                )
            }

            Column(modifier = Modifier.weight(1f)) {

                RegistrationField(
                    label = "Prezime *",
                    value = vm.prezime,
                    onValueChange = { vm.prezime = it },
                    error = vm.greske.prezime
                )
            }
        }

        // E-POSTA

        RegistrationField(
            label = "E-pošta *",
            value = vm.email,
            onValueChange = { vm.email = it },
            error = vm.greske.email,
            keyboardType = KeyboardType.Email
        )

        // LOZINKA

        RegistrationField(
            label = "Lozinka *",
            value = vm.lozinka,
            onValueChange = { vm.lozinka = it },
            error = vm.greske.lozinka,
            isPassword = true
        )

        // POTVRDA LOZINKE

        RegistrationField(
            label = "Potvrdi lozinku *",
            value = vm.potvrdaLozinke,
            onValueChange = { vm.potvrdaLozinke = it },
            error = vm.greske.potvrdaLozinke,
            isPassword = true
        )

        // KORISNICKA ULOGA

        Text(
            text = "Korisnička uloga *",
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            RadioButton(
                selected = vm.uloga == "Trener",
                onClick = {
                    vm.uloga = "Trener"
                }
            )

            Text("Trener")

            Spacer(modifier = Modifier.width(20.dp))

            RadioButton(
                selected = vm.uloga == "Sportaš",
                onClick = {
                    vm.uloga = "Sportaš"
                }
            )

            Text("Sportaš")
        }

        // POGRESKA KORISNICKE ULOGE

        vm.greske.uloga?.let { greska ->

            Text(
                text = greska,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // GUMB ZA REGISTRACIJU

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
                text = "REGISTRIRAJ SE",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // PORUKA NAKON PROVJERE PODATAKA

        if (vm.poruka.isNotEmpty()) {

            val imaGresaka = listOf(
                vm.greske.ime,
                vm.greske.prezime,
                vm.greske.email,
                vm.greske.lozinka,
                vm.greske.potvrdaLozinke,
                vm.greske.uloga
            ).any { it != null }

            Text(
                text = vm.poruka,
                color = if (imaGresaka) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}


// ZAJEDNICKA KOMPONENTA ZA POLJA UNOSA

@Composable
fun RegistrationField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = label,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(2.dp),

            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) {
                    KeyboardType.Password
                } else {
                    keyboardType
                }
            ),

            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            }
        )

        // Prikaz pogreske ispod polja

        error?.let { greska ->

            Text(
                text = greska,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
    }
}


// PREVIEW REGISTRACIJSKOG EKRANA

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {

    RegistrationScreen(
        vm = RegistrationViewModel()
    )
}