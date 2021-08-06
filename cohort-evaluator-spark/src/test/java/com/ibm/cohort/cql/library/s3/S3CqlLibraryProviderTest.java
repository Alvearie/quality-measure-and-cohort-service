package com.ibm.cohort.cql.library.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.cql.library.CqlLibraryProvider;

public class S3CqlLibraryProviderTest {

    @Test
    public void testListLibrariesSuccess() {
        String bucket = "bucket";
        String key = "key";
        AmazonS3 client = mock(AmazonS3.class);
        
        CqlLibraryDescriptor expectedCql = new CqlLibraryDescriptor()
                .setLibraryId("CohortHelpers")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);

        CqlLibraryDescriptor expectedElm = new CqlLibraryDescriptor()
                .setLibraryId("MyCQL")
                .setVersion("1.0.0")
                .setFormat(Format.ELM);
        
        List<CqlLibraryDescriptor> descriptors = Arrays.asList(expectedCql, expectedElm);
        
        ObjectListing objectListing = mock(ObjectListing.class);
        List<S3ObjectSummary> summaries = new ArrayList<>();
        for( CqlLibraryDescriptor d : descriptors ) {
            S3ObjectSummary summary = mock(S3ObjectSummary.class);
            when(summary.getKey()).thenReturn(generateFilename(d));
            summaries.add(summary);
        }
        when(objectListing.getObjectSummaries()).thenReturn(summaries);
        when(client.listObjects(bucket, key)).thenReturn(objectListing);
        
        CqlLibraryProvider provider = new S3CqlLibraryProvider(client, bucket, key);
        Collection<CqlLibraryDescriptor> libraries = provider.listLibraries();
        assertEquals(2, libraries.size());
        
        assertTrue("Missing expected library", libraries.contains(expectedCql));
        assertTrue("Missing expected library", libraries.contains(expectedElm));
    }
    
    @Test
    public void testGetLibrarySuccess() {
        String bucket = "bucket";
        String key = "key";
        AmazonS3 client = mock(AmazonS3.class);
        
        CqlLibraryDescriptor expectedDescriptor = new CqlLibraryDescriptor()
                .setLibraryId("CohortHelpers")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        expectedDescriptor.setExternalId(generateFilename(expectedDescriptor));
        
        CqlLibrary expectedLibrary = new CqlLibrary()
                .setDescriptor(expectedDescriptor)
                .setContent("library content");
        
        String objectName = String.format(key + "/" + expectedDescriptor.getExternalId());
        when(client.doesObjectExist(bucket, objectName)).thenReturn(Boolean.TRUE);
        when(client.getObjectAsString(bucket, objectName)).thenReturn(expectedLibrary.getContent());
        
        CqlLibraryProvider provider = new S3CqlLibraryProvider(client, bucket, key);
        CqlLibrary library = provider.getLibrary(expectedDescriptor);
        assertEquals( expectedLibrary, library );
    }

    protected String generateFilename(CqlLibraryDescriptor descriptor) {
        return String.format("%s-%s.%s", descriptor.getLibraryId(), descriptor.getVersion(), (descriptor.getFormat() == Format.CQL) ? "cql" : "xml");
    }
}
