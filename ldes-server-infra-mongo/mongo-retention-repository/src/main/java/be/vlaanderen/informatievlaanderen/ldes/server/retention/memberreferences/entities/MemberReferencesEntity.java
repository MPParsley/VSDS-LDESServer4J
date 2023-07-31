package be.vlaanderen.informatievlaanderen.ldes.server.retention.memberreferences.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("retention_member_references")
public class MemberReferencesEntity {
    @Id
    private final String id;
    @Indexed
    private final List<String> views;

    public MemberReferencesEntity(String id, List<String> views) {
        this.id = id;
        this.views = views;
    }

    public String getId() {
        return id;
    }

    public List<String> getViews() {
        return views;
    }
}
