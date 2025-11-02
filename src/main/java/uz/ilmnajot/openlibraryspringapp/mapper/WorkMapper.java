package uz.ilmnajot.openlibraryspringapp.mapper;

import org.springframework.stereotype.Component;
import uz.ilmnajot.openlibraryspringapp.entity.Work;
import uz.ilmnajot.openlibraryspringapp.model.WorkResponse;
import uz.ilmnajot.openlibraryspringapp.model.response.AuthorResponse;

@Component
public class WorkMapper {

    public WorkResponse toDto(Work work) {
        WorkResponse response = new WorkResponse();
        response.setWorkId(work.getWorkId());
        response.setTitle(work.getTitle());
        response.setDescription(work.getDescription());
        response.setSubjects(work.getSubjects());
        response.setCovers(work.getCovers());
        response.setAuthors(work
                .getAuthors()
                .stream()
                .map(AuthorResponse::from)
                .toList());
        return response;
    }
}
