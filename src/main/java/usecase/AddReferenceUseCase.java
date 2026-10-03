package usecase;

import domain.entity.Reference;

public class AddReferenceUseCase {

    public Reference execute(Reference reference) {

        if (reference.getReferenceName() == null || reference.getReferenceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Reference name cannot be empty.");
        }
        if (reference.getContactInfo() == null || reference.getContactInfo().trim().isEmpty()) {
            throw new IllegalArgumentException("Contact info cannot be empty.");
        }

        return reference;
    }
}