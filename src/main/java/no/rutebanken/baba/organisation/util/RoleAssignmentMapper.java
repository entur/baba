package no.rutebanken.baba.organisation.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import no.rutebanken.baba.exceptions.BabaException;
import no.rutebanken.baba.organisation.model.responsibility.EntityClassificationAssignment;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilityRoleAssignment;
import org.rutebanken.helper.organisation.RoleAssignment;

/**
 * Mapping utilities for RoleAssignment.
 */
public class RoleAssignmentMapper {

  /**
   * Convert a ResponsibilityRoleAssignment into its JSON representation.
   */
  public static String toAtr(ResponsibilityRoleAssignment roleAssignment) {
    RoleAssignment atr = toRoleAssignment(roleAssignment);

    try {
      ObjectMapper mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, atr);
      return writer.toString();
    } catch (IOException e) {
      throw new BabaException(e);
    }
  }

  /**
   * Convert a ResponsibilityRoleAssignment into a RoleAssignment.
   */
  public static RoleAssignment toRoleAssignment(
    ResponsibilityRoleAssignment responsibilityRoleAssignment
  ) {
    RoleAssignment atr = new RoleAssignment();
    atr.r = responsibilityRoleAssignment.getTypeOfResponsibilityRole().getPrivateCode();
    atr.o = responsibilityRoleAssignment.getResponsibleOrganisation().getPrivateCode();

    if (responsibilityRoleAssignment.getResponsibleArea() != null) {
      atr.z = responsibilityRoleAssignment.getResponsibleArea().getRoleAssignmentId();
    }

    responsibilityRoleAssignment
      .getResponsibleEntityClassifications()
      .forEach(ec -> addEntityClassification(atr, ec));

    return atr;
  }

  private static void addEntityClassification(
    RoleAssignment atr,
    EntityClassificationAssignment entityClassificationAssignment
  ) {
    if (atr.e == null) {
      atr.e = new HashMap<>();
    }

    String entityTypeRef = entityClassificationAssignment
      .getEntityClassification()
      .getEntityType()
      .getPrivateCode();
    List<String> entityClassificationsForEntityType = atr.e.computeIfAbsent(
      entityTypeRef,
      k -> new ArrayList<>()
    );

    // Represented negated entity classifications with '!' prefix for now. consider more structured representation.
    String classifierCode = entityClassificationAssignment
      .getEntityClassification()
      .getPrivateCode();
    if (!entityClassificationAssignment.isAllow()) {
      classifierCode = "!" + classifierCode;
    }

    entityClassificationsForEntityType.add(classifierCode);
  }
}
