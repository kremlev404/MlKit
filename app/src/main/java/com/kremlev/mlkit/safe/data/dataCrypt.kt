package com.kremlev.mlkit.safe.data

class dataCrypt(
        val key: String,
        val oldFilePath: String,
        val currentFilePath: String
) {
    constructor() : this("", "", "")
}