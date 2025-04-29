package org.egov.im.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

public class AdditionalDetailValidator  implements ConstraintValidator<CharacterConstraint, Object> {


    private Integer size;


    @Override
    public void initialize(CharacterConstraint additionalDetails) {
        size = additionalDetails.size();
    }

    @Override
    public boolean isValid(Object additionalDetails, ConstraintValidatorContext cxt) {

        if(additionalDetails==null)
            return true;

        if(additionalDetails.toString().length() > size)
            return false;
        else
            return true;
    }

}
