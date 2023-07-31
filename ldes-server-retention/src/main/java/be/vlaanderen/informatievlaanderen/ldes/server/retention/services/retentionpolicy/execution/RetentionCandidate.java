package be.vlaanderen.informatievlaanderen.ldes.server.retention.services.retentionpolicy.execution;

import be.vlaanderen.informatievlaanderen.ldes.server.retention.entities.MemberProperties;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.entities.MemberReferences;

public class RetentionCandidate {
   private final MemberProperties memberProperties;
   private final MemberReferences memberReferences;

    public RetentionCandidate(MemberProperties memberProperties, MemberReferences memberReferences) {
        this.memberProperties = memberProperties;
        this.memberReferences = memberReferences;
    }



    public MemberProperties getMemberProperties() {
        return memberProperties;
    }

    public void deleteViewReference(String viewName) {
        memberReferences.deleteView(viewName);
    }

    public String getId() {
        return memberReferences.getMemberId();
    }

    public boolean hasNoViewReferences() {
        return memberReferences.getViewReferences().isEmpty();
    }
}
