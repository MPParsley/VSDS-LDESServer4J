package be.vlaanderen.informatievlaanderen.ldes.server.retention.entities;

import java.util.List;

public class MemberReferences {
    private final String memberId;
    private final List<String> viewReferences;

    public MemberReferences(String memberId, List<String> viewReferences) {
        this.memberId = memberId;
        this.viewReferences = viewReferences;
    }

    public String getMemberId() {
        return memberId;
    }

    public List<String> getViewReferences() {
        return viewReferences;
    }

    public void deleteView(String viewName) {
        viewReferences.remove(viewName);
    }
}
