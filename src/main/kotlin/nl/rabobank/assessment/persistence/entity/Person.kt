package nl.rabobank.assessment.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import java.time.LocalDate
import java.util.*

data class Person(

    @Id
    private var id: UUID? = null,
    var firstName: String,
    var lastName: String,
    var dateOfBirth: LocalDate,
    var street: String,
    var houseNumber: Int,
    var houseNumberAdditions: String?,
    var postalCode: String,
    var city: String,
    var country: String

) : Persistable<UUID> {

    override fun getId(): UUID? {
        return id;
    }

    override fun isNew(): Boolean {
        if (id == null) {
            id = UUID.randomUUID()
            return true
        }
        return false
    }
}