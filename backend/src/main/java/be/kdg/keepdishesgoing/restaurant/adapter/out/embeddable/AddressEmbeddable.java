package be.kdg.keepdishesgoing.restaurant.adapter.out.embeddable;

import jakarta.persistence.Embeddable;

@Embeddable
public class AddressEmbeddable {
    private String street;
    private int number;
    private String postalCode;
    private String city;
    private String country;

    // Boxed and nullable: geocoding can fail, and rows written before it
    // existed have nothing here. Zero would be a real place off Africa.
    private Double latitude;
    private Double longitude;


    public AddressEmbeddable(String street, int number, String postalCode, String city, String country) {
        this(street, number, postalCode, city, country, null, null);
    }

    public AddressEmbeddable(String street, int number, String postalCode, String city, String country,
                             Double latitude, Double longitude) {
        this.street = street;
        this.number = number;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public AddressEmbeddable() {

    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
