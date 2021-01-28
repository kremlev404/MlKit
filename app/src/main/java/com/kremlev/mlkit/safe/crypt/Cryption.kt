package com.kremlev.mlkit.safe.crypt

import android.os.Environment
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kremlev.mlkit.safe.data.dataCrypt
import com.kremlev.mlkit.safe.fileNav.SafeState
import com.kremlev.mlkit.safe.fileNav.current
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.Key
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class Cryption(
        val path: String
) {

    //out flag to check file is not empty
    var CRYPT_ERROR = false

    //after we've generated key we have to
    //convert to string to load to firebase
    private fun decodeKey(key: Key): String {
        val keyString: String = Base64.getEncoder().encodeToString(key.getEncoded())
        return keyString
    }

    //after we got string frome base we have to convert it
    private fun stringToKey(keyStr: String): SecretKey {
        val decodedKey: ByteArray = Base64.getDecoder().decode(keyStr)
        return SecretKeySpec(decodedKey, 0,
                decodedKey.size, "AES")
    }

    //fun to generate key to encrypt
    private fun createKey(): Key {
        val keyGen = KeyGenerator.getInstance("AES")
        val keyGenerated: Key = keyGen.generateKey()
        return keyGenerated
    }

    private fun generateName(): String = List(18) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")

    //main encrypt fun
    fun encrypt() {
        //Flag
        CRYPT_ERROR = false

        if (File(path).exists() && File(path).isFile) {
            CRYPT_ERROR = false

            //create file name to enctypted file
            val newpath = Environment.getExternalStorageDirectory()!!.absolutePath + "/.MLSafe/.Vault/" +
                    "/${generateName()}"

            //firebase set path and key value
            var mPathReference: DatabaseReference = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child(newpath.substringAfterLast("/"))

            val keyToUse = createKey()

            //push data to db
            mPathReference.push()
                    .setValue(
                            dataCrypt(decodeKey(keyToUse), path, newpath)
                    )
            //cipher init
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, keyToUse)

            try {
                //create read stream
                val cipt = CipherInputStream(FileInputStream(File(path)), cipher)

                //create outFileStream to save file
                val out = FileOutputStream(File(newpath))
                var i: Int

                while (cipt.read().also { i = it } != -1) {
                    out.write(i)
                }

                //close streams
                cipt.close()
                out.close()

                //delete file
                File(path).delete()
                Log.e("<ENCRYPT>", "SUCCESS $newpath SAVING")


            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            CRYPT_ERROR = true
        }
    }

    //decrypt will call decryptToFile to save file
    fun decrypt() {
        var haveError = false
        if (File(path).exists() && File(path).isFile) {
            //init firebase ref
            var mPathReference: DatabaseReference = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child(path.substringAfterLast("/"))

            //var to get data
            var receivedData: dataCrypt = dataCrypt()

            mPathReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds: DataSnapshot in dataSnapshot.children) {
                        receivedData = (ds.getValue(dataCrypt::class.java)!!)
                    }

                    if (receivedData.key.isNotEmpty()) {
                        val job = SupervisorJob()
                        val scope = CoroutineScope(Dispatchers.Default + job)

                        scope.launch {
                            withContext(Dispatchers.Main) {
                                //set loading gui
                                SafeState.isProcessing = true
                                SafeState.refresh()

                                //encrypt in background
                                withContext(Dispatchers.IO) {

                                    decryptToFile(receivedData)

                                    withContext(Dispatchers.Main) {
                                        //disable loading ui
                                        SafeState.isProcessing = false

                                        //remove data frome firebase
                                        mPathReference.removeValue()
                                        SafeState.refresh()
                                        current.refresh()
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.e("veListener", "Failed to read value.", error.toException())
                }
            })
        }
    }


    //main decrypt fun
    fun decryptToFile(receivedData: dataCrypt) {
        //path to restore
        var newpath = receivedData.oldFilePath

        //check if file that we wanna restore already exist
        if (!(File(newpath).isFile) && !(File(newpath).exists())) {

            //init cipher
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, stringToKey(receivedData.key))

            try {

                //stream to read
                val cipherStream = CipherInputStream(FileInputStream(File(path)), cipher)

                //stream to save
                val cipherOutStream = FileOutputStream(File(newpath))
                var j: Int
                while (cipherStream.read().also { j = it } != -1)
                    cipherOutStream.write(j)

                cipherStream.close()
                cipherOutStream.close()

                //delete encrypted file
                File(path).delete()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            Log.e("DECRYPT", "SUCCESS $newpath SAVING")

        } else {
            Log.e("DECRYPT", "ERROR $newpath SAVING")
        }
    }
}