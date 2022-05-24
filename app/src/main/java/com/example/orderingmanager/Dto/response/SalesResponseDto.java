package com.example.orderingmanager.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static lombok.AccessLevel.PROTECTED;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class SalesResponseDto {

    private String sales;

    public String getSales() {
        return sales;
    }
    public String setSales(String sales){
        this.sales = sales;
        return sales;
    }

}
