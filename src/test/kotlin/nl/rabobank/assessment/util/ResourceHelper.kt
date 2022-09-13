package nl.rabobank.assessment.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.IOException
import java.io.UncheckedIOException
import java.util.*

class ResourceHelper {

    companion object {


        private var objectMapper: ObjectMapper? = null

        init
        {
            val mapper = ObjectMapper().registerKotlinModule()
            mapper.registerModule(JavaTimeModule ())
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            objectMapper = mapper
        }

        fun <T> getResourceAsType(fileLocation: String?, returnType: Class<T>?): T {
            try {
                Objects.requireNonNull(ResourceHelper::class.java.classLoader.getResourceAsStream(fileLocation))
                    .use { input -> return objectMapper!!.readValue(input, returnType) }
            } catch (e: IOException) {
                throw UncheckedIOException(e.message, e)
            }
        }
    }
}