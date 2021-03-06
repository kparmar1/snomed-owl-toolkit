package org.snomed.otf.owltoolkit;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.snomed.otf.owltoolkit.constants.Concepts;
import org.snomed.otf.owltoolkit.conversion.AxiomRelationshipConversionService;
import org.snomed.otf.owltoolkit.conversion.ConversionException;
import org.snomed.otf.owltoolkit.domain.AxiomRepresentation;
import org.snomed.otf.owltoolkit.domain.Relationship;
import org.snomed.otf.owltoolkit.ontology.OntologyService;

import java.util.*;

import static org.junit.Assert.*;

public class AxiomRelationshipConversionServiceTest {

	private AxiomRelationshipConversionService axiomRelationshipConversionService;

	@Before
	public void setup() {
		HashSet<Long> ungroupedAttributes = Sets.newHashSet(Concepts.LATERALITY_LONG);
		axiomRelationshipConversionService = new AxiomRelationshipConversionService(ungroupedAttributes);
	}

	@Test
	public void testGCITwoGroupsOneRelationshipInEach() throws ConversionException {
		String axiom =
				"SubClassOf(" +
					"ObjectIntersectionOf(" +
						":73211009 " +
						"ObjectSomeValuesFrom(" +
							":609096000 " +
							"ObjectSomeValuesFrom(" +
								":100105001 " +
								":100101001" +
							")" +
						")" +
					") " +
					":8801005" +
				")";

		AxiomRepresentation representation = axiomRelationshipConversionService.convertAxiomToRelationships(axiom);

		assertEquals(true, representation.isPrimitive());

		assertEquals(
				"0 116680003=73211009\n" +
				"1 100105001=100101001",
				toString(representation.getLeftHandSideRelationships()));

		assertEquals(8801005, representation.getRightHandSideNamedConcept().longValue());

		// Test converting relationships back to an axiom
		String recreatedAxiom = axiomRelationshipConversionService.convertRelationshipsToAxiom(representation);
		assertEquals(axiom, recreatedAxiom);
	}

	@Test
	public void testAdditionalAxiomPrimitiveTwoGroupsOneRelationshipInEach() throws ConversionException {
		String axiom =
				"SubClassOf(" +
					":8801005 " +
					"ObjectIntersectionOf(" +
						":73211009 " +
						"ObjectSomeValuesFrom(" +
							":609096000 " +
							"ObjectSomeValuesFrom(" +
								":100105001 " +
								":100101001" +
							")" +
						")" +
					")" +
				")";

		AxiomRepresentation representation = axiomRelationshipConversionService.convertAxiomToRelationships(axiom);

		assertEquals(true, representation.isPrimitive());

		assertEquals(8801005, representation.getLeftHandSideNamedConcept().longValue());

		assertEquals(
				"0 116680003=73211009\n" +
				"1 100105001=100101001",
				toString(representation.getRightHandSideRelationships()));

		// Test converting relationships back to an axiom
		String recreatedAxiom = axiomRelationshipConversionService.convertRelationshipsToAxiom(representation);
		assertEquals(axiom, recreatedAxiom);
	}

	@Test
	public void testAdditionalAxiomSufficientlyDefinedTwoRelationshipsInGroup() throws ConversionException {
		String axiom =
				"EquivalentClasses(" +
					":10002003 " +
					"ObjectIntersectionOf(" +
						":116175006 " +
						"ObjectSomeValuesFrom(" +
							":609096000 " +
							"ObjectIntersectionOf(" +
								"ObjectSomeValuesFrom(" +
									":260686004 " +
									":129304002" +
								") " +
								"ObjectSomeValuesFrom(" +
									":405813007 " +
									":414003" +
								")" +
							")" +
						")" +
					")" +
				")";

		AxiomRepresentation representation = axiomRelationshipConversionService.convertAxiomToRelationships(axiom);

		assertEquals(false, representation.isPrimitive());

		assertEquals(10002003, representation.getLeftHandSideNamedConcept().longValue());

		assertEquals(
				"0 116680003=116175006\n" +
				"1 260686004=129304002\n" +
				"1 405813007=414003",
				toString(representation.getRightHandSideRelationships()));

		// Test converting relationships back to an axiom
		String recreatedAxiom = axiomRelationshipConversionService.convertRelationshipsToAxiom(representation);
		assertEquals(axiom, recreatedAxiom);
	}

	@Test
	public void testAdditionalAxiomPrimitiveWithSingleRelationship() throws ConversionException {
		String axiom = "SubClassOf(:118956008 :123037004)";

		AxiomRepresentation representation = axiomRelationshipConversionService.convertAxiomToRelationships(axiom);

		assertEquals(118956008, representation.getLeftHandSideNamedConcept().longValue());

		assertEquals(
				"0 116680003=123037004",
				toString(representation.getRightHandSideRelationships()));

		assertEquals(118956008, representation.getLeftHandSideNamedConcept().longValue());

		// Test converting relationships back to an axiom
		String recreatedAxiom = axiomRelationshipConversionService.convertRelationshipsToAxiom(representation);
		assertEquals(axiom, recreatedAxiom);
		assertEquals(true, representation.isPrimitive());
	}

	@Test
	public void testAxiomPrimitiveWithSingleRelationshipWithoutGivingReferencedComponentId() throws ConversionException {
		String axiom = "SubClassOf(:118956008 :123037004)";

		AxiomRepresentation representation = axiomRelationshipConversionService.convertAxiomToRelationships(axiom);

		assertEquals(
				"0 116680003=123037004",
				toString(representation.getRightHandSideRelationships()));

		assertEquals(118956008, representation.getLeftHandSideNamedConcept().longValue());

		// Test converting relationships back to an axiom
		String recreatedAxiom = axiomRelationshipConversionService.convertRelationshipsToAxiom(representation);
		assertEquals(axiom, recreatedAxiom);
	}

	@Test
	public void testAttributeIsARelationship() throws ConversionException {
		String axiom = "SubObjectPropertyOf(:363698007 :762705008)";

		AxiomRepresentation representation = axiomRelationshipConversionService.convertAxiomToRelationships(axiom);

		assertNotNull(representation);

		assertEquals(
				"0 116680003=762705008",
				toString(representation.getRightHandSideRelationships()));

		assertEquals(363698007, representation.getLeftHandSideNamedConcept().longValue());
		assertEquals(true, representation.isPrimitive());
	}

	@Test
	public void testAttributePropertyChain() throws ConversionException {
		String axiom = "SubObjectPropertyOf(ObjectPropertyChain(:246093002 :738774007) :246093002)";

		AxiomRepresentation representation = axiomRelationshipConversionService.convertAxiomToRelationships(axiom);

		assertNull(representation);
	}

	@Test
	public void testAdditionalAxiomNeverGrouped() throws ConversionException {
		String axiom =
				"EquivalentClasses(" +
					":9846003 " +
					"ObjectIntersectionOf(" +
						":39132006 " +
						":64033007 " +
						"ObjectSomeValuesFrom(" +
							":272741003 " +
							":24028007" +
						")" +
					")" +
				")";

		AxiomRepresentation representation = axiomRelationshipConversionService.convertAxiomToRelationships(axiom);

		assertEquals(9846003L, representation.getLeftHandSideNamedConcept().longValue());

		assertEquals(false, representation.isPrimitive());

		assertEquals(
				"0 116680003=39132006\n" +
				"0 116680003=64033007\n" +
				"0 272741003=24028007",
				toString(representation.getRightHandSideRelationships()));

		// Test converting relationships back to an axiom
		String recreatedAxiom = axiomRelationshipConversionService.convertRelationshipsToAxiom(representation);
		assertEquals(axiom, recreatedAxiom);
	}

	@Test
	public void testConvertTransitiveObjectPropertyReturnNull() throws ConversionException {
		AxiomRepresentation axiomRepresentation = axiomRelationshipConversionService.convertAxiomToRelationships("TransitiveObjectProperty(:738774007)");
		assertNull(axiomRepresentation);
	}

	@Test
	public void testGetIdsOfConceptsNamedInAxiom() throws ConversionException {
		assertEquals(Sets.newHashSet(733930001L), axiomRelationshipConversionService.getIdsOfConceptsNamedInAxiom("TransitiveObjectProperty(:733930001)"));

		assertEquals(Sets.newHashSet(738774007L), axiomRelationshipConversionService.getIdsOfConceptsNamedInAxiom("ReflexiveObjectProperty(:738774007)"));

		assertEquals(Sets.newHashSet(246093002L, 738774007L),
				axiomRelationshipConversionService.getIdsOfConceptsNamedInAxiom("SubObjectPropertyOf(ObjectPropertyChain(:246093002 :738774007) :246093002)"));

		String equivalentClassAxiom =
				"EquivalentClasses(" +
						":9846003 " +
						"ObjectIntersectionOf(" +
						":39132006 " +
						":64033007 " +
						"ObjectSomeValuesFrom(" +
						":272741003 " +
						":24028007" +
						")" +
						")" +
						" )";
		assertEquals(Sets.newHashSet(9846003L, 39132006L, 64033007L, 272741003L, 24028007L), axiomRelationshipConversionService.getIdsOfConceptsNamedInAxiom(equivalentClassAxiom));
	}

	@Test
	public void testConvertRelationshipsToAxiomAllowGroupedAttribute() {
		AxiomRepresentation representation = new AxiomRepresentation();
		representation.setLeftHandSideNamedConcept(9846003L);
		representation.setRightHandSideRelationships(toMap(
				new Relationship(Concepts.IS_A_LONG, 39132006L),
				// Put a grouped attribute in group 1
				new Relationship(1, Concepts.HAS_ACTIVE_INGREDIENT_LONG, 7771000L)));

		String actual = axiomRelationshipConversionService.convertRelationshipsToAxiom(representation);
		assertTrue(actual.contains(OntologyService.ROLE_GROUP_SCTID));
		assertEquals("EquivalentClasses(:9846003 ObjectIntersectionOf(:39132006 ObjectSomeValuesFrom(:609096000 ObjectSomeValuesFrom(:127489000 :7771000))))", actual);
	}

	@Test
	public void testConvertRelationshipsToAxiomMoveUngroupedAttribute() {
		AxiomRepresentation representation = new AxiomRepresentation();
		representation.setLeftHandSideNamedConcept(9846003L);
		representation.setRightHandSideRelationships(toMap(
				new Relationship(Concepts.IS_A_LONG, 39132006L),
				// Attempt to group an ungroupable attribute by placing it in group 1
				new Relationship(1, Concepts.LATERALITY_LONG, 7771000L)));

		String actual = axiomRelationshipConversionService.convertRelationshipsToAxiom(representation);
		assertFalse(actual.contains(OntologyService.ROLE_GROUP_SCTID));
		assertEquals("EquivalentClasses(:9846003 ObjectIntersectionOf(:39132006 ObjectSomeValuesFrom(:272741003 :7771000)))", actual);
	}

	private Map<Integer, List<Relationship>> toMap(Relationship... relationships) {
		HashMap<Integer, List<Relationship>> relationshipMap = new HashMap<>();
		for (Relationship relationship : relationships) {
			relationshipMap.computeIfAbsent(relationship.getGroup(), g -> new ArrayList<>()).add(relationship);
		}
		return relationshipMap;
	}

	private String toString(Map<Integer, List<Relationship>> relationshipGroups) {
		StringBuilder groupsString = new StringBuilder();
		for (Integer group : relationshipGroups.keySet()) {
			List<Relationship> relationships = relationshipGroups.get(group);
			for (Relationship relationship : relationships) {
				groupsString.append(relationship.getGroup())
						.append(" ")
						.append(relationship.getTypeId())
						.append("=")
						.append(relationship.getDestinationId());
				groupsString.append("\n");
			}
		}
		if (groupsString.length() > 0) {
			groupsString.deleteCharAt(groupsString.length() - 1);
		}
		return groupsString.toString();
	}


}
