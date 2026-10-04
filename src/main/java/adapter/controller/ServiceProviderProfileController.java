package adapter.controller;

import domain.entity.Reference;
import domain.entity.Review;
import usecase.AddReferenceUseCase;
import usecase.GetProviderProfileUseCase;

import java.util.List;

public class ServiceProviderProfileController {

    private final AddReferenceUseCase addReferenceUseCase;
    private final GetProviderProfileUseCase getProviderProfileUseCase;

    public ServiceProviderProfileController() {
        this.addReferenceUseCase = new AddReferenceUseCase();
        this.getProviderProfileUseCase = new GetProviderProfileUseCase();
    }

    public Reference addReference(Reference reference) {
        return addReferenceUseCase.execute(reference);
    }

    public GetProviderProfileUseCase.ProfileData getProfile(String providerId, List<Reference> references, List<Review> reviews) {
        return getProviderProfileUseCase.execute(providerId, references, reviews);
    }
}
