package usecase;

import domain.entity.ShopOwner;

public class RegisterShopOwnerUseCase {

    public ShopOwner execute(ShopOwner shopOwner) {
        shopOwner.setApproved(false);
        return shopOwner;
    }
}
