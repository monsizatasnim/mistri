package adapter.controller;

import domain.entity.ShopOwner;
import usecase.RegisterShopOwnerUseCase;

public class ShopOwnerController {

    private final RegisterShopOwnerUseCase registerShopOwnerUseCase;

    public ShopOwnerController() {
        this.registerShopOwnerUseCase = new RegisterShopOwnerUseCase();
    }

    public ShopOwner registerShopOwner(ShopOwner shopOwner) {
        return registerShopOwnerUseCase.execute(shopOwner);
    }

}
