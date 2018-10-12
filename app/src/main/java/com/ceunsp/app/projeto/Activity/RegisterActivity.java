package com.ceunsp.app.projeto.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.ceunsp.app.projeto.Model.User;
import com.ceunsp.app.projeto.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
    private EditText emailEdit, passwordEdit, nameEdit, ageEdit;
    private Button saveButton;
    private Spinner collegeSpinner, courseSpinner;
    private String array_spinner[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameEdit        = findViewById(R.id.name_edit);
        ageEdit         = findViewById(R.id.age_edit);
        collegeSpinner  = findViewById(R.id.college_spinner);
        courseSpinner   = findViewById(R.id.course_spinner);
        emailEdit       = findViewById(R.id.email_edit);
        passwordEdit    = findViewById(R.id.password_edit);
        saveButton      = findViewById(R.id.save_button);

        getSupportActionBar().setTitle("Novo usuário");

        LoadSpinner();

        /* Cadastro do usuario no Firebase Authentication */
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (AttempRegister()){
                    auth.createUserWithEmailAndPassword(emailEdit.getText().toString(), passwordEdit.getText().toString())
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){

                                        String userID = auth.getCurrentUser().getUid();
                                        User user = new User();
                                        user.setName(nameEdit.getText().toString());
                                        ref.child(userID).setValue(user);
                                        auth.signOut();
                                        finish();

                                    }else{

                                    }
                                }
                            });
                }
            }
        });

    }


    //Validações dos campos obrigatórios
    public boolean AttempRegister(){

        emailEdit.setError(null);
        passwordEdit.setError(null);
        nameEdit.setError(null);

        String emailText    = emailEdit.getText().toString();
        String passwordText = passwordEdit.getText().toString();
        String nameText     = nameEdit.getText().toString();
        boolean cancel      = false;
        View focusView      = null;

        // Verifica se é um email válido.
        if (TextUtils.isEmpty(emailText)) {
            emailEdit.setError(getString(R.string.error_field_required));
            focusView = emailEdit;
            cancel = true;
        } else if (!isValidEmail(emailText)) {
            emailEdit.setError(getString(R.string.error_invalid_email));
            focusView = emailEdit;
            cancel = true;
        }

        //Verifica se o nome é valido
        if (TextUtils.isEmpty(nameText)) {
            nameEdit.setError(getString(R.string.error_field_required));
            focusView = nameEdit;
            cancel = true;
        } else if (!isValidName(nameText)){
            nameEdit.setError(getString(R.string.error_invalid_name));
            focusView = nameEdit;
            cancel = true;
        }

        // Verifica se a senha é valida
        if (TextUtils.isEmpty(passwordText)){
            passwordEdit.setError(getString(R.string.error_field_required));
            focusView = passwordEdit;
            cancel = true;
        } else if (!isValidPassword(passwordText)){
            passwordEdit.setError(getString(R.string.error_invalid_password));
            focusView = passwordEdit;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
            return false;

        } else {
            return true;
        }
    }

    public final static boolean isValidPassword(String target) {
        return Pattern.compile("^(?=.*\\d)(?=.*[a-zA-Z])[a-zA-Z0-9]{4,12}$").matcher(target).matches();
    }

    private boolean isValidEmail(String email) {
        return email.contains("@");
    }

    public final static boolean isValidName(String target) {
        return Pattern.compile("^(?=.*[a-zA-Z가-힣])[a-zA-Z가-힣]{1,}$").matcher(target).matches();

    }

    public final static boolean isValidNickName(String target) {
        return Pattern.compile("^(?=.*[a-zA-Z\\d])[a-zA-Z0-9가-힣]{2,12}$|^[가-힣]$").matcher(target).matches();
    }

    private void LoadSpinner(){
        String []collegeData = getResources().getStringArray(R.array.colleges);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,collegeData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        collegeSpinner.setAdapter(adapter);

        String []coursesData = getResources().getStringArray(R.array.courses);
        ArrayAdapter<String> adapter2 =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,coursesData);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(adapter2);
    }
}
