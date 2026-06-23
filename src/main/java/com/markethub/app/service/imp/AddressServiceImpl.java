package com.markethub.app.service.imp;

import com.markethub.app.exception.ResourceNotFoundException;
import com.markethub.app.model.Address;
import com.markethub.app.repository.AddressRepository;
import com.markethub.app.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> getAllAddress() {
        return addressRepository.findAll();
    }

    @Override
    @Transactional
    public Address createAddress(Address address) {
        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAddressById(Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Address updateAddress(Address address, Long id) {
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));
        existing.setStreet(address.getStreet());
        existing.setCity(address.getCity());
        existing.setState(address.getState());
        existing.setZipCode(address.getZipCode());
        existing.setAddressType(address.getAddressType());
        existing.setPhoneNumber(address.getPhoneNumber());
        existing.setUser(address.getUser());
        return addressRepository.save(existing);
    }
}
