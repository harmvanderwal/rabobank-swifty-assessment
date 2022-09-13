package nl.rabobank.assessment.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import java.util.*

data class Pet(

    @Id
    private var id: UUID? = null,
    var name: String,
    var age: Int,
    var personId: UUID? = null

) : Persistable<UUID> {
    override fun getId(): UUID? {
        return id
    }

    override fun isNew(): Boolean {
        if (id == null) {
            id = UUID.randomUUID()
            return true
        }
        return false
    }
}
