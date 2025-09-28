package com.swp.project.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.address.ProvinceCity;
import com.swp.project.service.AddressService;


@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/address")
public class AdminAddressController {
    
    private final AddressService addressService;

    @GetMapping("/provinces")
    public List<ProvinceCity> getAllProvinceCity() {
        return addressService.getAllProvinceCity();
    }

    @GetMapping("/provinces/{provinceCityCode}")
    public ProvinceCity getProvinceCityByCode(@PathVariable String provinceCityCode) {
        return addressService.getProvinceCityByCode(provinceCityCode);
    }


    @GetMapping("/wards/{provinceCityCode}")
    public List<CommuneWard> getAllCommuneWards(@PathVariable String provinceCityCode) {
        return addressService.getAllCommuneWardByProvinceCityCode(provinceCityCode);
    }

    @GetMapping("/ward/{code}")
    public CommuneWard getCommuneWardByCode(@PathVariable String code) {
        return addressService.getCommuneWardByCode(code);
    }

}