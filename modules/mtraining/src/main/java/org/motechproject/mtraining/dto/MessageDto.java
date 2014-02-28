package org.motechproject.mtraining.dto;

/**
 * Object representing a message in a course structure.
 * Expected by {@link org.motechproject.mtraining.service.CourseService} APIs to manage a mTraining {@link org.motechproject.mtraining.domain.Message}.
 */

public class MessageDto {
    private String name;
    private String externalId;
    private String description;

    public MessageDto() {
    }

    public MessageDto(String name, String externalId, String description) {
        this.name = name;
        this.externalId = externalId;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getDescription() {
        return description;
    }
}