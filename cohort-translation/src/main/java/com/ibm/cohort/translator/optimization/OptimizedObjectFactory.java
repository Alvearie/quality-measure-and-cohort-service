/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.ibm.cohort.translator.optimization;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.cqframework.cql.elm.execution.*;
import org.opencds.cqf.cql.engine.elm.execution.ObjectFactoryEx;

import com.ibm.cohort.annotations.Generated;

@SuppressWarnings("RedundantMethodOverride")
@Generated
@XmlRegistry
public class OptimizedObjectFactory extends ObjectFactoryEx {

	@Override
	public And createAnd() {
		return new ShortAndEvaluator();
	}

	@Override
	public Or createOr() {
		return new ShortOrEvaluator();
	}

	// The JAXB implementations does not recursively search the superclass for annotations
	@Override
	@XmlElementDecl(namespace = "urn:hl7-org:elm:r1", name = "library")
	public JAXBElement<Library> createLibrary(Library value) {
		return new JAXBElement<>(new QName("urn:hl7-org:elm:r1", "library"), Library.class, null, value);
	}

	/**
	 *
	 * The remaining methods are auto-generated stubs from ObjectFactoryEx.
	 * They need to be declared redundantly,
	 * as {@link org.eclipse.persistence.jaxb.compiler.AnnotationsProcessor#processObjectFactory}
	 * only processes declared methods from an object factory
	 *
	 */

	@Override
	public Abs createAbs() {
		return super.createAbs();
	}

	@Override
	public Add createAdd() {
		return super.createAdd();
	}

	@Override
	public After createAfter() {
		return super.createAfter();
	}

	@Override
	public AliasRef createAliasRef() {
		return super.createAliasRef();
	}

	@Override
	public AllTrue createAllTrue() {
		return super.createAllTrue();
	}

	@Override
	public AnyTrue createAnyTrue() {
		return super.createAnyTrue();
	}

	@Override
	public AnyInValueSet createAnyInValueSet() {
		return super.createAnyInValueSet();
	}

	@Override
	public As createAs() {
		return super.createAs();
	}

	@Override
	public Avg createAvg() {
		return super.createAvg();
	}

	@Override
	public Before createBefore() {
		return super.createBefore();
	}

	@Override
	public CalculateAge createCalculateAge() {
		return super.createCalculateAge();
	}

	@Override
	public CalculateAgeAt createCalculateAgeAt() {
		return super.createCalculateAgeAt();
	}

	@Override
	public Case createCase() {
		return super.createCase();
	}

	@Override
	public Ceiling createCeiling() {
		return super.createCeiling();
	}

	@Override
	public Children createChildren() {
		return super.createChildren();
	}

	@Override
	public Coalesce createCoalesce() {
		return super.createCoalesce();
	}

	@Override
	public Code createCode() {
		return super.createCode();
	}

	@Override
	public CodeDef createCodeDef() {
		return super.createCodeDef();
	}

	@Override
	public CodeRef createCodeRef() {
		return super.createCodeRef();
	}

	@Override
	public CodeSystemDef createCodeSystemDef() {
		return super.createCodeSystemDef();
	}

	@Override
	public CodeSystemRef createCodeSystemRef() {
		return super.createCodeSystemRef();
	}

	@Override
	public Collapse createCollapse() {
		return super.createCollapse();
	}

	@Override
	public Combine createCombine() {
		return super.createCombine();
	}

	@Override
	public Concatenate createConcatenate() {
		return super.createConcatenate();
	}

	@Override
	public Concept createConcept() {
		return super.createConcept();
	}

	@Override
	public ConceptDef createConceptDef() {
		return super.createConceptDef();
	}

	@Override
	public ConceptRef createConceptRef() {
		return super.createConceptRef();
	}

	@Override
	public Contains createContains() {
		return super.createContains();
	}

	@Override
	public Convert createConvert() {
		return super.createConvert();
	}

	@Override
	public ConvertQuantity createConvertQuantity() {
		return super.createConvertQuantity();
	}

	@Override
	public ConvertsToBoolean createConvertsToBoolean() {
		return super.createConvertsToBoolean();
	}

	@Override
	public ConvertsToDate createConvertsToDate() {
		return super.createConvertsToDate();
	}

	@Override
	public ConvertsToDateTime createConvertsToDateTime() {
		return super.createConvertsToDateTime();
	}

	@Override
	public ConvertsToDecimal createConvertsToDecimal() {
		return super.createConvertsToDecimal();
	}

	@Override
	public ConvertsToInteger createConvertsToInteger() {
		return super.createConvertsToInteger();
	}

	@Override
	public ConvertsToQuantity createConvertsToQuantity() {
		return super.createConvertsToQuantity();
	}

	@Override
	public ConvertsToString createConvertsToString() {
		return super.createConvertsToString();
	}

	@Override
	public ConvertsToTime createConvertsToTime() {
		return super.createConvertsToTime();
	}

	@Override
	public Count createCount() {
		return super.createCount();
	}

	@Override
	public Date createDate() {
		return super.createDate();
	}

	@Override
	public DateTime createDateTime() {
		return super.createDateTime();
	}

	@Override
	public DateFrom createDateFrom() {
		return super.createDateFrom();
	}

	@Override
	public DateTimeComponentFrom createDateTimeComponentFrom() {
		return super.createDateTimeComponentFrom();
	}

	@Override
	public Descendents createDescendents() {
		return super.createDescendents();
	}

	@Override
	public DifferenceBetween createDifferenceBetween() {
		return super.createDifferenceBetween();
	}

	@Override
	public Distinct createDistinct() {
		return super.createDistinct();
	}

	@Override
	public Divide createDivide() {
		return super.createDivide();
	}

	@Override
	public DurationBetween createDurationBetween() {
		return super.createDurationBetween();
	}

	@Override
	public End createEnd() {
		return super.createEnd();
	}

	@Override
	public Ends createEnds() {
		return super.createEnds();
	}

	@Override
	public EndsWith createEndsWith() {
		return super.createEndsWith();
	}

	@Override
	public Equal createEqual() {
		return super.createEqual();
	}

	@Override
	public Equivalent createEquivalent() {
		return super.createEquivalent();
	}

	@Override
	public Except createExcept() {
		return super.createExcept();
	}

	@Override
	public Exists createExists() {
		return super.createExists();
	}

	@Override
	public Exp createExp() {
		return super.createExp();
	}

	@Override
	public Expand createExpand() {
		return super.createExpand();
	}

	@Override
	public ExpressionDef createExpressionDef() {
		return super.createExpressionDef();
	}

	@Override
	public ExpressionRef createExpressionRef() {
		return super.createExpressionRef();
	}

	@Override
	public Filter createFilter() {
		return super.createFilter();
	}

	@Override
	public First createFirst() {
		return super.createFirst();
	}

	@Override
	public Flatten createFlatten() {
		return super.createFlatten();
	}

	@Override
	public Floor createFloor() {
		return super.createFloor();
	}

	@Override
	public ForEach createForEach() {
		return super.createForEach();
	}

	@Override
	public FunctionRef createFunctionRef() {
		return super.createFunctionRef();
	}

	@Override
	public GeometricMean createGeometricMean() {
		return super.createGeometricMean();
	}

	@Override
	public Greater createGreater() {
		return super.createGreater();
	}

	@Override
	public GreaterOrEqual createGreaterOrEqual() {
		return super.createGreaterOrEqual();
	}

	@Override
	public HighBoundary createHighBoundary() {
		return super.createHighBoundary();
	}

	@Override
	public IdentifierRef createIdentifierRef() {
		return super.createIdentifierRef();
	}

	@Override
	public If createIf() {
		return super.createIf();
	}

	@Override
	public Implies createImplies() {
		return super.createImplies();
	}

	@Override
	public IncludedIn createIncludedIn() {
		return super.createIncludedIn();
	}

	@Override
	public Includes createIncludes() {
		return super.createIncludes();
	}

	@Override
	public Indexer createIndexer() {
		return super.createIndexer();
	}

	@Override
	public IndexOf createIndexOf() {
		return super.createIndexOf();
	}

	@Override
	public In createIn() {
		return super.createIn();
	}

	@Override
	public InCodeSystem createInCodeSystem() {
		return super.createInCodeSystem();
	}

	@Override
	public InValueSet createInValueSet() {
		return super.createInValueSet();
	}

	@Override
	public Instance createInstance() {
		return super.createInstance();
	}

	@Override
	public Intersect createIntersect() {
		return super.createIntersect();
	}

	@Override
	public Interval createInterval() {
		return super.createInterval();
	}

	@Override
	public Is createIs() {
		return super.createIs();
	}

	@Override
	public IsFalse createIsFalse() {
		return super.createIsFalse();
	}

	@Override
	public IsNull createIsNull() {
		return super.createIsNull();
	}

	@Override
	public IsTrue createIsTrue() {
		return super.createIsTrue();
	}

	@Override
	public Last createLast() {
		return super.createLast();
	}

	@Override
	public LastPositionOf createLastPositionOf() {
		return super.createLastPositionOf();
	}

	@Override
	public Length createLength() {
		return super.createLength();
	}

	@Override
	public Less createLess() {
		return super.createLess();
	}

	@Override
	public LessOrEqual createLessOrEqual() {
		return super.createLessOrEqual();
	}

	@Override
	public List createList() {
		return super.createList();
	}

	@Override
	public Literal createLiteral() {
		return super.createLiteral();
	}

	@Override
	public Ln createLn() {
		return super.createLn();
	}

	@Override
	public Log createLog() {
		return super.createLog();
	}

	@Override
	public LowBoundary createLowBoundary() {
		return super.createLowBoundary();
	}

	@Override
	public Lower createLower() {
		return super.createLower();
	}

	@Override
	public Matches createMatches() {
		return super.createMatches();
	}

	@Override
	public MaxValue createMaxValue() {
		return super.createMaxValue();
	}

	@Override
	public Max createMax() {
		return super.createMax();
	}

	@Override
	public Median createMedian() {
		return super.createMedian();
	}

	@Override
	public Meets createMeets() {
		return super.createMeets();
	}

	@Override
	public MeetsAfter createMeetsAfter() {
		return super.createMeetsAfter();
	}

	@Override
	public MeetsBefore createMeetsBefore() {
		return super.createMeetsBefore();
	}

	@Override
	public Message createMessage() {
		return super.createMessage();
	}

	@Override
	public MinValue createMinValue() {
		return super.createMinValue();
	}

	@Override
	public Min createMin() {
		return super.createMin();
	}

	@Override
	public Mode createMode() {
		return super.createMode();
	}

	@Override
	public Modulo createModulo() {
		return super.createModulo();
	}

	@Override
	public Multiply createMultiply() {
		return super.createMultiply();
	}

	@Override
	public Negate createNegate() {
		return super.createNegate();
	}

	@Override
	public NotEqual createNotEqual() {
		return super.createNotEqual();
	}

	@Override
	public Not createNot() {
		return super.createNot();
	}

	@Override
	public Now createNow() {
		return super.createNow();
	}

	@Override
	public Null createNull() {
		return super.createNull();
	}

	@Override
	public OperandRef createOperandRef() {
		return super.createOperandRef();
	}

	@Override
	public Overlaps createOverlaps() {
		return super.createOverlaps();
	}

	@Override
	public OverlapsBefore createOverlapsBefore() {
		return super.createOverlapsBefore();
	}

	@Override
	public OverlapsAfter createOverlapsAfter() {
		return super.createOverlapsAfter();
	}

	@Override
	public ParameterRef createParameterRef() {
		return super.createParameterRef();
	}

	@Override
	public PointFrom createPointFrom() {
		return super.createPointFrom();
	}

	@Override
	public PopulationStdDev createPopulationStdDev() {
		return super.createPopulationStdDev();
	}

	@Override
	public PopulationVariance createPopulationVariance() {
		return super.createPopulationVariance();
	}

	@Override
	public PositionOf createPositionOf() {
		return super.createPositionOf();
	}

	@Override
	public Power createPower() {
		return super.createPower();
	}

	@Override
	public Precision createPrecision() {
		return super.createPrecision();
	}

	@Override
	public Predecessor createPredecessor() {
		return super.createPredecessor();
	}

	@Override
	public Product createProduct() {
		return super.createProduct();
	}

	@Override
	public ProperContains createProperContains() {
		return super.createProperContains();
	}

	@Override
	public ProperIn createProperIn() {
		return super.createProperIn();
	}

	@Override
	public ProperIncludes createProperIncludes() {
		return super.createProperIncludes();
	}

	@Override
	public ProperIncludedIn createProperIncludedIn() {
		return super.createProperIncludedIn();
	}

	@Override
	public Property createProperty() {
		return super.createProperty();
	}

	@Override
	public Quantity createQuantity() {
		return super.createQuantity();
	}

	@Override
	public Query createQuery() {
		return super.createQuery();
	}

	@Override
	public QueryLetRef createQueryLetRef() {
		return super.createQueryLetRef();
	}

	@Override
	public Ratio createRatio() {
		return super.createRatio();
	}

	@Override
	public Repeat createRepeat() {
		return super.createRepeat();
	}

	@Override
	public ReplaceMatches createReplaceMatches() {
		return super.createReplaceMatches();
	}

	@Override
	public Retrieve createRetrieve() {
		return super.createRetrieve();
	}

	@Override
	public Round createRound() {
		return super.createRound();
	}

	@Override
	public SameAs createSameAs() {
		return super.createSameAs();
	}

	@Override
	public SameOrAfter createSameOrAfter() {
		return super.createSameOrAfter();
	}

	@Override
	public SameOrBefore createSameOrBefore() {
		return super.createSameOrBefore();
	}

	@Override
	public SingletonFrom createSingletonFrom() {
		return super.createSingletonFrom();
	}

	@Override
	public Size createSize() {
		return super.createSize();
	}

	@Override
	public Slice createSlice() {
		return super.createSlice();
	}

	@Override
	public Split createSplit() {
		return super.createSplit();
	}

	@Override
	public SplitOnMatches createSplitOnMatches() {
		return super.createSplitOnMatches();
	}

	@Override
	public Start createStart() {
		return super.createStart();
	}

	@Override
	public Starts createStarts() {
		return super.createStarts();
	}

	@Override
	public StartsWith createStartsWith() {
		return super.createStartsWith();
	}

	@Override
	public StdDev createStdDev() {
		return super.createStdDev();
	}

	@Override
	public Substring createSubstring() {
		return super.createSubstring();
	}

	@Override
	public Subtract createSubtract() {
		return super.createSubtract();
	}

	@Override
	public Successor createSuccessor() {
		return super.createSuccessor();
	}

	@Override
	public Sum createSum() {
		return super.createSum();
	}

	@Override
	public Time createTime() {
		return super.createTime();
	}

	@Override
	public TimeOfDay createTimeOfDay() {
		return super.createTimeOfDay();
	}

	@Override
	public TimeFrom createTimeFrom() {
		return super.createTimeFrom();
	}

	@Override
	public TimezoneOffsetFrom createTimezoneOffsetFrom() {
		return super.createTimezoneOffsetFrom();
	}

	@Override
	public Today createToday() {
		return super.createToday();
	}

	@Override
	public ToBoolean createToBoolean() {
		return super.createToBoolean();
	}

	@Override
	public ToConcept createToConcept() {
		return super.createToConcept();
	}

	@Override
	public ToDecimal createToDecimal() {
		return super.createToDecimal();
	}

	@Override
	public ToDate createToDate() {
		return super.createToDate();
	}

	@Override
	public ToDateTime createToDateTime() {
		return super.createToDateTime();
	}

	@Override
	public ToInteger createToInteger() {
		return super.createToInteger();
	}

	@Override
	public ToList createToList() {
		return super.createToList();
	}

	@Override
	public ToQuantity createToQuantity() {
		return super.createToQuantity();
	}

	@Override
	public ToRatio createToRatio() {
		return super.createToRatio();
	}

	@Override
	public ToString createToString() {
		return super.createToString();
	}

	@Override
	public ToTime createToTime() {
		return super.createToTime();
	}

	@Override
	public TruncatedDivide createTruncatedDivide() {
		return super.createTruncatedDivide();
	}

	@Override
	public Truncate createTruncate() {
		return super.createTruncate();
	}

	@Override
	public Tuple createTuple() {
		return super.createTuple();
	}

	@Override
	public Union createUnion() {
		return super.createUnion();
	}

	@Override
	public Upper createUpper() {
		return super.createUpper();
	}

	@Override
	public Variance createVariance() {
		return super.createVariance();
	}

	@Override
	public ValueSetRef createValueSetRef() {
		return super.createValueSetRef();
	}

	@Override
	public Width createWidth() {
		return super.createWidth();
	}

	@Override
	public Xor createXor() {
		return super.createXor();
	}

}
