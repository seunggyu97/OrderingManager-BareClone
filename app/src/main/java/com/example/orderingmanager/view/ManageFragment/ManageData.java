package com.example.orderingmanager.view.ManageFragment;

public class ManageData {

    private String iv_menu;
    private String name;
    private String price;
    private String intro;
    private Long foodId;

    public ManageData(Long foodId, String iv_menu, String name, String price, String intro) {
        this.foodId = foodId;
        this.iv_menu = iv_menu;
        this.name = name;
        this.price = price;
        this.intro = intro;

    }

    public Long getFoodId() { return foodId;}

    public void setFoodId(Long id){this.foodId = id;}

    public String getIv_menu() {
        return iv_menu;
    }

    public void setIv_menu(String iv_menu) {
        this.iv_menu = iv_menu;
    }

    public String getName() {
        return name;
    }

    public void setName(String tv_name) {
        this.name = tv_name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }
}