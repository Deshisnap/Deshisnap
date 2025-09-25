package com.deshisnap; // Ensure this is your correct package name

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ContactUsActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput, messageInput;
    private Button submitButton;

    // Hardcoded admin email address
    private static final String ADMIN_EMAIL = "deshisnap247service@gmail.com"; // Updated admin email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        nameInput = findViewById(R.id.contact_name_input);
        emailInput = findViewById(R.id.contact_email_input);
        phoneInput = findViewById(R.id.contact_phone_input);
        messageInput = findViewById(R.id.contact_message_input);
        submitButton = findViewById(R.id.submit_contact_button);

        submitButton.setOnClickListener(v -> sendEmail());
    }

    private void sendEmail() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String subject = "Contact Us Message from " + name;
        String emailBody = "Name: " + name + "\n" +
                "Email: " + email + "\n" +
                "Phone: " + phone + "\n\n" +
                "Message:\n" + message;

        // 1) Preferred: ACTION_SENDTO with mailto:
        Intent sendTo = new Intent(Intent.ACTION_SENDTO);
        sendTo.setData(Uri.parse("mailto:"));
        sendTo.putExtra(Intent.EXTRA_EMAIL, new String[]{ADMIN_EMAIL});
        sendTo.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendTo.putExtra(Intent.EXTRA_TEXT, emailBody);
        if (sendTo.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(sendTo, "Send email using..."));
            clearForm();
            return;
        }

        // 2) Try explicit Gmail app
        Intent gmailIntent = new Intent(Intent.ACTION_SENDTO);
        gmailIntent.setData(Uri.parse("mailto:" + ADMIN_EMAIL));
        gmailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        gmailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
        gmailIntent.setPackage("com.google.android.gm");
        if (gmailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(gmailIntent);
            clearForm();
            return;
        }

        // 3) Try ACTION_VIEW with a mailto URI including query params
        String mailto = "mailto:" + ADMIN_EMAIL
                + "?subject=" + Uri.encode(subject)
                + "&body=" + Uri.encode(emailBody);
        Intent viewMailto = new Intent(Intent.ACTION_VIEW, Uri.parse(mailto));
        if (viewMailto.resolveActivity(getPackageManager()) != null) {
            startActivity(viewMailto);
            clearForm();
            return;
        }

        // 4) Fallback: open Gmail web compose in browser
        String webCompose = "https://mail.google.com/mail/?view=cm&fs=1&to=" + Uri.encode(ADMIN_EMAIL)
                + "&su=" + Uri.encode(subject)
                + "&body=" + Uri.encode(emailBody);
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(webCompose));
        if (browser.resolveActivity(getPackageManager()) != null) {
            startActivity(browser);
            clearForm();
            return;
        }

        // 5) As a last resort, nudge user to install an email app (Gmail)
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.google.android.gm")));
        } catch (Exception ignored) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gm")));
        }
        Toast.makeText(this, "No email app found. Redirecting to Play Store.", Toast.LENGTH_LONG).show();
    }

    private void clearForm() {
        Toast.makeText(this, "Opening email...", Toast.LENGTH_SHORT).show();
        nameInput.setText("");
        emailInput.setText("");
        phoneInput.setText("");
        messageInput.setText("");
    }
}