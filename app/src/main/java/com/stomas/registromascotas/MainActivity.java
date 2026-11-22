package com.stomas.registromascotas;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText codigo, nombre, dueno, direccion;
    private Spinner tipo;
    private TextView lista;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 100, 32, 60);
        scroll.addView(layout);
        setContentView(scroll);

        TextView titulo = new TextView(this);
        titulo.setText("Registro de mascotas");
        titulo.setTextSize(26);
        layout.addView(titulo);

        codigo = campo(layout, "Código");
        nombre = campo(layout, "Nombre de la mascota");

        tipo = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Perro", "Gato", "Pájaro"}
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        tipo.setAdapter(adapter);
        layout.addView(tipo);

        dueno = campo(layout, "Nombre del dueño");
        direccion = campo(layout, "Dirección");

        Button enviar = new Button(this);
        enviar.setText("Enviar datos");
        layout.addView(enviar);

        Button cargar = new Button(this);
        cargar.setText("Cargar datos");
        layout.addView(cargar);

        lista = new TextView(this);
        lista.setTextSize(18);
        layout.addView(lista);

        enviar.setOnClickListener(v -> {
            if (texto(codigo).isEmpty() || texto(nombre).isEmpty()
                    || texto(dueno).isEmpty()
                    || texto(direccion).isEmpty()) {
                mensaje("Completa todos los campos");
                return;
            }

            Map<String, Object> datos = new HashMap<>();
            datos.put("codigo", texto(codigo));
            datos.put("nombre", texto(nombre));
            datos.put("tipo", tipo.getSelectedItem().toString());
            datos.put("dueno", texto(dueno));
            datos.put("direccion", texto(direccion));

            enviar.setEnabled(false);

            db.collection("mascotas").add(datos)
                    .addOnSuccessListener(resultado -> {
                        enviar.setEnabled(true);
                        mensaje("Mascota guardada en Firebase");
                        codigo.setText("");
                        nombre.setText("");
                        dueno.setText("");
                        direccion.setText("");
                        cargarDatos();
                    })
                    .addOnFailureListener(error -> {
                        enviar.setEnabled(true);
                        mensaje("Error: " + error.getMessage());
                    });
        });

        cargar.setOnClickListener(v -> cargarDatos());
    }

    private EditText campo(LinearLayout layout, String pista) {
        EditText edit = new EditText(this);
        edit.setHint(pista);
        edit.setSingleLine(true);
        layout.addView(edit);
        return edit;
    }

    private String texto(EditText campo) {
        return campo.getText().toString().trim();
    }

    private void mensaje(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
    }

    private void cargarDatos() {
        lista.setText("Cargando...");

        db.collection("mascotas").get()
                .addOnSuccessListener(resultado -> {
                    StringBuilder texto = new StringBuilder();

                    for (DocumentSnapshot doc : resultado.getDocuments()) {
                        texto.append("\nCódigo: ")
                                .append(doc.getString("codigo"))
                                .append("\nNombre: ")
                                .append(doc.getString("nombre"))
                                .append("\nTipo: ")
                                .append(doc.getString("tipo"))
                                .append("\nDueño: ")
                                .append(doc.getString("dueno"))
                                .append("\nDirección: ")
                                .append(doc.getString("direccion"))
                                .append("\n\n");
                    }

                    lista.setText(resultado.isEmpty()
                            ? "No hay mascotas registradas."
                            : texto.toString());
                })
                .addOnFailureListener(error -> {
                    lista.setText("Error: " + error.getMessage());
                });
    }
}