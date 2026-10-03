package usecase;

import domain.entity.Reference;
import domain.entity.Review;
import java.util.List;

public class GetProviderProfileUseCase {

    public ProfileData execute(String providerId, List<Reference> references, List<Review> reviews) {
        return new ProfileData(providerId, references, reviews);
    }

    public static class ProfileData {
        private String providerId;
        private List<Reference> references;
        private List<Review> reviews;

        public ProfileData(String providerId, List<Reference> references, List<Review> reviews) {
            this.providerId = providerId;
            this.references = references;
            this.reviews = reviews;
        }

        public String getProviderId() { return providerId; }
        public List<Reference> getReferences() { return references; }
        public List<Review> getReviews() { return reviews; }
    }
}
