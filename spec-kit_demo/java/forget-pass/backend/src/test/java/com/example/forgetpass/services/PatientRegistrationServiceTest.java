package com.example.forgetpass.services;

import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.domain.PatientCredential;
import com.example.forgetpass.repositories.PatientCredentialRepository;
import com.example.forgetpass.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PatientRegistrationServiceTest {

    @Mock PatientRepository patientRepository;
    @Mock PatientCredentialRepository credentialRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailValidationService emailValidationService;
    @Mock PasswordValidationService passwordValidationService;

    @InjectMocks PatientRegistrationService service;

    private final String email = "john.smith@example.com";
    private final String phone = "+1 (555) 123-4567";
    private final String strongPassword = "Str0ngP@ssw0rd!";

    @BeforeEach
    void setup() {
        // no global stubbing; each test will stub only what it uses to avoid UnnecessaryStubbingException
    }

    @Test
    void register_throwsInvalidEmail_whenFormatBad() {
        when(emailValidationService.isValidFormat(email)).thenReturn(false);
        when(emailValidationService.isDisposable(email)).thenReturn(false);
        when(passwordValidationService.meetsPolicy(strongPassword)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () ->
                service.register("John", "Smith", email, phone, LocalDate.now().minusYears(30), strongPassword));
    }

    @Test
    void register_throwsInvalidEmail_whenDisposableDomain() {
        when(emailValidationService.isValidFormat(email)).thenReturn(true);
        when(emailValidationService.isDisposable(email)).thenReturn(true);
        when(passwordValidationService.meetsPolicy(strongPassword)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () ->
                service.register("John", "Smith", email, phone, LocalDate.now().minusYears(30), strongPassword));
    }

    @Test
    void register_throwsUnderage_whenDobLessThan18Years() {
        when(emailValidationService.isValidFormat(email)).thenReturn(true);
        when(emailValidationService.isDisposable(email)).thenReturn(false);
        when(passwordValidationService.meetsPolicy(strongPassword)).thenReturn(true);
        LocalDate under18 = LocalDate.now().minusYears(17);
        assertThrows(IllegalArgumentException.class, () ->
                service.register("John", "Smith", email, phone, under18, strongPassword));
    }

    @Test
    void register_throwsWeakPassword_whenPolicyNotMet() {
        when(emailValidationService.isValidFormat(email)).thenReturn(true);
        when(emailValidationService.isDisposable(email)).thenReturn(false);
        when(passwordValidationService.meetsPolicy("weakpassword")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () ->
                service.register("John", "Smith", email, phone, LocalDate.now().minusYears(30), "weakpassword"));
    }

    @Test
    void register_throwsEmailAlreadyExists_whenDuplicateEmail() {
        when(emailValidationService.isValidFormat(email)).thenReturn(true);
        when(emailValidationService.isDisposable(email)).thenReturn(false);
        when(passwordValidationService.meetsPolicy(strongPassword)).thenReturn(true);
        Patient existing = new Patient(); existing.setEmail(email);
        when(patientRepository.findByEmail(email)).thenReturn(Optional.of(existing));
        assertThrows(IllegalStateException.class, () ->
                service.register("John", "Smith", email, phone, LocalDate.now().minusYears(30), strongPassword));
    }

    @Test
    void register_savesPatient_andCredential_withEncodedPassword() {
        LocalDate dob = LocalDate.now().minusYears(25);
        when(emailValidationService.isValidFormat(email)).thenReturn(true);
        when(emailValidationService.isDisposable(email)).thenReturn(false);
        when(passwordValidationService.meetsPolicy(strongPassword)).thenReturn(true);
        when(patientRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(strongPassword)).thenReturn("BC_HASH");
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(credentialRepository.save(any(PatientCredential.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        ArgumentCaptor<PatientCredential> credCaptor = ArgumentCaptor.forClass(PatientCredential.class);

        Patient saved = service.register("John", "Smith", email, phone, dob, strongPassword);
        assertThat(saved).isNotNull();
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Smith");
        assertThat(saved.getPhoneNumber()).isEqualTo(phone);
        assertThat(saved.getDateOfBirth()).isEqualTo(dob);

        verify(patientRepository).save(patientCaptor.capture());
        Patient toSave = patientCaptor.getValue();
        assertThat(toSave.getEmail()).isEqualTo(email);

        verify(credentialRepository).save(credCaptor.capture());
        PatientCredential credSaved = credCaptor.getValue();
        assertThat(credSaved.getPatient()).isEqualTo(saved);
        assertThat(credSaved.getPasswordHash()).isEqualTo("BC_HASH");

        verify(passwordEncoder).encode(strongPassword);
        verify(patientRepository).findByEmail(email);
    }
}
