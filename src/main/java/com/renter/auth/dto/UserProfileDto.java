package com.renter.auth.dto;

public class UserProfileDto {
    
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String phoneNumber;
    private String country;
    private String emergencyContactPhone;
    private String emergencyContactName;
    private String idProofNumber;
    private String idProofType;
    private String postalCode;
    private String state;


    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getIdProofNumber() {
        return idProofNumber;
    }

    public void setIdProofNumber(String idProofNumber) {
        this.idProofNumber = idProofNumber;
    }

    public String getIdProofType() {
        return idProofType;
    }

    public void setIdProofType(String idProofType) {
        this.idProofType = idProofType;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "UserProfile{" +
                "addressLine1='" + addressLine1 + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", city='" + city + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", country='" + country + '\'' +
                ", emergencyContactPhone='" + emergencyContactPhone + '\'' +
                ", emergencyContactName='" + emergencyContactName + '\'' +
                ", idProofNumber='" + idProofNumber + '\'' +
                ", idProofType='" + idProofType + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
