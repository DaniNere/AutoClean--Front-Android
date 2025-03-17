package com.example.autoclean.ui.auth.documents

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.autoclean.R
import com.example.autoclean.data.api.ApiClient
import com.example.autoclean.data.model.dto.UpdateDocumentsDto
import com.example.autoclean.databinding.FragmentCrlvUploadBinding
import com.example.autoclean.databinding.FragmentSelfieWithCnhBinding
import com.example.autoclean.ui.auth.documents.SelfieWithCNHFragment.Companion
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CRLVUploadFragment : Fragment() {

    private var _binding: FragmentCrlvUploadBinding? = null
    private val binding get() = _binding!!
    private var imageUrl: String? = null
    private lateinit var photoUri: Uri

    companion object {
        private const val TAG = "CRLVFragment"
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val storageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true

        when {
            cameraGranted -> openCamera()
            storageGranted -> openDocumentPicker()
            else -> {
                Log.d(TAG, "Permissions denied for camera or storage.")
                Toast.makeText(requireContext(), "Permissões negadas. Não é possível abrir a câmera ou galeria.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Log.d(TAG, "Foto capturada com sucesso: $photoUri")
            displayImagePreview(photoUri)
            uploadImageToFirebase(photoUri)
        } else {
            Log.d(TAG, "Falha ao capturar a foto.")
            showToast("Não foi possível capturar a foto.")
        }
    }

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d(TAG, "Imagem selecionada: $uri")
                displayImagePreview(uri)
                uploadImageToFirebase(uri)
            } ?: showToast("Falha ao processar a imagem selecionada.")
        } else {
            showToast("Nenhum documento selecionado.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrlvUploadBinding.inflate(inflater, container, false)

        binding.btnEnviar.isEnabled = false

        binding.uploadLinearLayout.setOnClickListener { handleActionButtonClick() }

        binding.btnEnviar.setOnClickListener {
            if (!imageUrl.isNullOrEmpty()) {
                updateProfilePictureInDatabase("19", imageUrl!!)
            } else {
                showToast("A URL da imagem ainda não está pronta.")
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun handleActionButtonClick() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openDocumentPicker()
        } else {
            requestPermissionsIfNeeded()
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
            Log.d(TAG, "Camera permission needed.")
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            Log.d(TAG, "Storage permission needed.")
        }

        if (permissionsToRequest.isNotEmpty()) {
            Log.d(TAG, "Solicitando permissões.")
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )
            Log.d(TAG, "URI da foto criada: $photoUri")
            takePictureLauncher.launch(photoUri)
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao criar arquivo para imagem: ${e.message}")
            Toast.makeText(requireContext(), "Erro ao criar arquivo para imagem.", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(null) ?: requireContext().filesDir
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            Log.d(TAG, "Arquivo de imagem criado: $absolutePath")
        }
    }

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        selectImageLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(uri: Uri) {
        Log.d(TAG, "Inicio do upload para Firebase com URI: $uri")
        val storageReference = Firebase.storage.reference
        val imageRef = storageReference.child("images/${uri.lastPathSegment}")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                Log.d(TAG, "Upload bem-sucedido")
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrl = downloadUri.toString()
                    Log.d(TAG, "URL da imagem no Firebase: $imageUrl")
                    showToast("Upload realizado com sucesso!")
                    binding.btnEnviar.isEnabled = true
                }
            }.addOnFailureListener { e ->
                Log.d(TAG, "Falha ao enviar a imagem: ${e.message}")
                showToast("Falha ao enviar a imagem.")
            }
    }

    private fun updateProfilePictureInDatabase(userId: String, imageUrl: String) {
        Log.d(TAG, "Atualizando URL da imagem no banco de dados.")
        val updateDocumentsDto = UpdateDocumentsDto(crlvUrl = imageUrl)
        Log.d(TAG, "URL obtida após o upload: $updateDocumentsDto")

        ApiClient.apiService.updateCrlvPhoto(userId, updateDocumentsDto).enqueue(object :
            Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("URL salva no banco de dados com sucesso.")
                } else {
                    showToast("Falha ao salvar URL no banco de dados.")
                }
                response.errorBody()?.close()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Erro de conexão com o servidor.")
            }
        })
    }

    private fun displayImagePreview(uri: Uri) {
        Log.d(TAG, "Mostrando preview da imagem: $uri")
        binding.imagePreview.apply {
            setImageURI(uri)
            visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        Log.d(TAG, "Mostrando Toast: $message")
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}