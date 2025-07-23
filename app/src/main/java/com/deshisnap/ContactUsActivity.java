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
    private static final String ADMIN_EMAIL = "pradhansambit2005@.com"; // CHANGE THIS TO YOUR ADMIN EMAIL

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

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // Only email apps should handle this.
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ADMIN_EMAIL});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
            Toast.makeText(this, "Opening email client...", Toast.LENGTH_SHORT).show();
            // Clear fields after sending attempt
            nameInput.setText("");
            emailInput.setText("");
            phoneInput.setText("");
            messageInput.setText("");
        } else {
            Toast.makeText(this, "No email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}