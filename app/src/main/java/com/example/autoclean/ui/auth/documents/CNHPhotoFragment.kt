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
import com.example.autoclean.databinding.FragmentCnhPhotoBinding
import com.example.autoclean.databinding.FragmentSelfieWithCnhBinding
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

class CNHPhotoFragment : Fragment() {
    private var _binding: FragmentCnhPhotoBinding? = null
    private val binding get() = _binding!!

    private var frontCnhUrl: String? = null
    private var backCnhUrl: String? = null
    private lateinit var currentPhotoUri: Uri
    private var isFrontImage: Boolean = true
    private var frontImageUri: Uri? = null
    private var backImageUri: Uri? = null


    companion object {
        private const val TAG = "CNHFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCnhPhotoBinding.inflate(inflater, container, false)

        binding.btnEnviar.isEnabled = true

        binding.frontImageLayout.setOnClickListener {
            isFrontImage = true
            handleActionButtonClick()
        }

        binding.backImageLayout.setOnClickListener {
            isFrontImage = false
            handleActionButtonClick()
        }

        binding.btnEnviar.setOnClickListener {
            Log.d(TAG, "Valores atuais - Front URL: $frontCnhUrl, Back URL: $backCnhUrl")
            if (frontCnhUrl.isNullOrEmpty()) {
                showToast("A imagem da frente não está pronta.")
            } else if (backCnhUrl.isNullOrEmpty()) {
                showToast("A imagem do verso não está pronta.")
            } else {
                updateProfilePicturesInDatabase("18", frontCnhUrl!!, backCnhUrl!!)
            }
        }

        return binding.root
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
                showToast("Permissões negadas. Não é possível abrir a câmera ou galeria.")
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Log.d(TAG, "Foto capturada com sucesso: $currentPhotoUri")
            displayImagePreview(currentPhotoUri)
            storeImageUri(uri = currentPhotoUri)
        } else {
            Log.d(TAG, "Falha ao capturar a foto.")
            showToast("Não foi possível capturar a foto.")
        }
    }
    private fun storeImageUri(uri: Uri) {
        if (isFrontImage) {
            frontImageUri = currentPhotoUri
            Log.d(TAG, "URI frontal armazenada: $frontImageUri")
        } else {
            backImageUri = currentPhotoUri
            Log.d(TAG, "URI traseira armazenada: $backImageUri")
        }

        if (frontImageUri != null && backImageUri != null) {
            uploadImagesToFirebase()
        }
    }

    private fun uploadImagesToFirebase() {
        frontImageUri?.let { uri ->
            uploadSingleImageToFirebase(uri, true)
        }

        backImageUri?.let { uri ->
            uploadSingleImageToFirebase(uri, false)
        }
    }


    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d(TAG, "Imagem selecionada: $uri")
                displayImagePreview(uri)  // Exibir a imagem imediatamente
                storeImageUri(uri)
            } ?: showToast("Falha ao processar a imagem selecionada.")
        } else {
            showToast("Nenhum documento selecionado.")
        }
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
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )
            Log.d(TAG, "URI da foto criada: $currentPhotoUri")
            takePictureLauncher.launch(currentPhotoUri)
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao criar arquivo para imagem: ${e.message}")
            showToast("Erro ao criar arquivo para imagem.")
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

    private fun uploadSingleImageToFirebase(uri: Uri, isFront: Boolean) {
        val storageReference = Firebase.storage.reference
        val imageRef = storageReference.child("images/${uri.lastPathSegment}")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                Log.d(TAG, "Upload bem-sucedido")
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    if (isFront) {
                        frontCnhUrl = downloadUri.toString()
                        Log.d(TAG, "URL da imagem frontal no Firebase: $frontCnhUrl")
                    } else {
                        backCnhUrl = downloadUri.toString()
                        Log.d(TAG, "URL da imagem traseira no Firebase: $backCnhUrl")
                    }
                    checkUrlsAndSaveToDatabase()
                }
            }.addOnFailureListener { e ->
                Log.d(TAG, "Falha ao enviar a imagem: ${e.message}")
                showToast("Falha ao enviar a imagem.")
            }
    }

    private fun checkUrlsAndSaveToDatabase() {
        if (!frontCnhUrl.isNullOrEmpty() && !backCnhUrl.isNullOrEmpty()) {
            updateProfilePicturesInDatabase("14", frontCnhUrl!!, backCnhUrl!!)
        }
    }

    private fun updateProfilePicturesInDatabase(userId: String, frontCnhUrl: String, backCnhUrl: String) {
        Log.d(TAG, "Atualizando URL das imagens no banco de dados.")


        val updateDocumentsDto = UpdateDocumentsDto(
            frontCnhUrl = frontCnhUrl,
            backCnhUrl = backCnhUrl
        )
        Log.d(TAG, "URLs obtidas após o upload: $updateDocumentsDto")


        ApiClient.apiService.updateCnhPhoto(userId, updateDocumentsDto).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showImageUploadConfirmationDialog()
                } else {
                    showToast("Falha ao salvar URLs no banco de dados.")
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

        if (isFrontImage) {
            binding.imagePreview1.apply {
                setImageURI(uri)
                visibility = View.VISIBLE
            }
            Log.d(TAG, "Imagem frontal exibida")
        } else {
            binding.imagePreview2.apply {
                setImageURI(uri)
                visibility = View.VISIBLE
            }
            Log.d(TAG, "Imagem traseira exibida")
        }
    }

    private fun showToast(message: String) {
        Log.d(TAG, "Mostrando Toast: $message")
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showImageUploadConfirmationDialog() {
        val dialog = AlertModal.newInstance()
        dialog.show(parentFragmentManager, "ImageUploadConfirmationDialog")
    }

}