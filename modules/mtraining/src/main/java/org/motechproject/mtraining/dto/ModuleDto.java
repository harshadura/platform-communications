package org.motechproject.mtraining.dto;

import java.util.List;

/**
 * Object representing a module in a course structure.
 * Expected by {@link org.motechproject.mtraining.service.CourseService} APIs to manage a mTraining {@link org.motechproject.mtraining.domain.Module}.
 */

public class ModuleDto {
    private String name;
    private String description;
    private List<ChapterDto> chapters;

    public ModuleDto() {
    }

    public ModuleDto(String name, String description, List<ChapterDto> chapters) {
        this.name = name;
        this.description = description;
        this.chapters = chapters;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ChapterDto> getChapters() {
        return chapters;
    }
}