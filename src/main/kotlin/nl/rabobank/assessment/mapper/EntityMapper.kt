package nl.rabobank.assessment.mapper

import nl.rabobank.assessment.persistence.entity.Person
import nl.rabobank.assessment.persistence.entity.Pet
import nl.rabobank.assessment.ui.rest.model.request.PersonRequest
import nl.rabobank.assessment.ui.rest.model.request.PetRequest
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse
import nl.rabobank.assessment.ui.rest.model.response.PetResponse
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface EntityMapper {

    @Mapping(target = "id", ignore = true)
    fun toPerson(personRequest: PersonRequest): Person

    fun toPersonResponse(person: Person): PersonResponse

    @Mapping(target = "id", ignore = true)
    fun toPet(petRequest: PetRequest): Pet

    fun toPet(@MappingTarget pet: Pet, petRequest: PetRequest): Pet

    fun toPetResponse(pet: Pet): PetResponse?

    @Mapping(target = "lastName", ignore = true)
	@Mapping(target = "firstName", ignore = true)
	@Mapping(target = "dateOfBirth", ignore = true)
    fun updatePersonAddress(@MappingTarget person: Person, updateAddressRequest: UpdateAddressRequest?): Person
}