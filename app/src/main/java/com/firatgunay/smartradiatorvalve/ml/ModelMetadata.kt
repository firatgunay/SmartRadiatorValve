package com.firatgunay.smartradiatorvalve.ml

data class ModelMetadata(
    val inputShape: IntArray,
    val outputShape: IntArray,
    val inputMin: Float,
    val inputMax: Float,
    val outputMin: Float,
    val outputMax: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelMetadata

        if (!inputShape.contentEquals(other.inputShape)) return false
        if (!outputShape.contentEquals(other.outputShape)) return false
        if (inputMin != other.inputMin) return false
        if (inputMax != other.inputMax) return false
        if (outputMin != other.outputMin) return false
        if (outputMax != other.outputMax) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inputShape.contentHashCode()
        result = 31 * result + outputShape.contentHashCode()
        result = 31 * result + inputMin.hashCode()
        result = 31 * result + inputMax.hashCode()
        result = 31 * result + outputMin.hashCode()
        result = 31 * result + outputMax.hashCode()
        return result
    }
} 