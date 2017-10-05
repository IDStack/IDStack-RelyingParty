package org.idstack.relyingparty.response;

/**
 * @author Sachithra Dangalla
 * @date 10/5/2017
 * @since 1.0
 */
public class CorrelationScoreResponse {
    private SuperAttribute name;
    private SuperAttribute address;
    private SuperAttribute dob;
    private SuperAttribute gender;
    private SuperAttribute nic;

    public CorrelationScoreResponse(SuperAttribute name, SuperAttribute address, SuperAttribute dob, SuperAttribute gender, SuperAttribute nic) {
        this.name = name;
        this.address = address;
        this.dob = dob;
        this.gender = gender;
        this.nic = nic;
    }
}
