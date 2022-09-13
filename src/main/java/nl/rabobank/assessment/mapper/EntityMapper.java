package nl.rabobank.assessment.mapper;

import nl.rabobank.assessment.ui.rest.model.request.PersonRequest;
import nl.rabobank.assessment.ui.rest.model.request.PetRequest;
import nl.rabobank.assessment.ui.rest.model.request.UpdateAddressRequest;
import nl.rabobank.assessment.ui.rest.model.response.PersonResponse;
import nl.rabobank.assessment.ui.rest.model.response.PetResponse;
import nl.rabobank.assessment.persistence.entity.Person;
import nl.rabobank.assessment.persistence.entity.Pet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EntityMapper {

	Person toPerson(PersonRequest personRequest);

	PersonResponse toPersonResponse(Person person);

	Pet toPet(PetRequest petRequest);

	Pet toPet(@MappingTarget Pet pet, PetRequest petRequest);

	PetResponse toPetResponse(Pet pet);

	@Mapping(target = "lastName", ignore = true)
	@Mapping(target = "firstName", ignore = true)
	@Mapping(target = "dateOfBirth", ignore = true)
	Person updatePersonAddress(@MappingTarget Person person, UpdateAddressRequest updateAddressRequest);
}
