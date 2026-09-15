package com.neuronovaapps.mimomento

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupRulesTest {

    private val projectRoot: File = File(".").canonicalFile.let { dir ->
        if (dir.name == "app") dir.parentFile else dir
    }

    @Test
    fun manifest_hasAllowBackupTrueAndReferencesBackupRules() {
        val manifestFile = File(projectRoot, "app/src/main/AndroidManifest.xml")
        assertTrue("AndroidManifest.xml should exist", manifestFile.exists())
        val content = manifestFile.readText()

        assertTrue("android:allowBackup must be true", content.contains("android:allowBackup=\"true\""))
        assertTrue(
            "android:dataExtractionRules must point to @xml/data_extraction_rules",
            content.contains("android:dataExtractionRules=\"@xml/data_extraction_rules\"")
        )
        assertTrue(
            "android:fullBackupContent must point to @xml/backup_rules",
            content.contains("android:fullBackupContent=\"@xml/backup_rules\"")
        )
    }

    @Test
    fun dataExtractionRules_excludesJournalAndIncludesProgressAndDataStore() {
        val rulesFile = File(projectRoot, "app/src/main/res/xml/data_extraction_rules.xml")
        assertTrue("data_extraction_rules.xml should exist", rulesFile.exists())
        val content = rulesFile.readText()

        // Excludes
        assertTrue(
            "data_extraction_rules must exclude journal_entries.json",
            content.contains("""<exclude domain="file" path="journal_entries.json" />""")
        )
        assertTrue(
            "data_extraction_rules must exclude journal_entries.json.bak",
            content.contains("""<exclude domain="file" path="journal_entries.json.bak" />""")
        )

        // Includes
        assertTrue(
            "data_extraction_rules must include datastore",
            content.contains("""<include domain="file" path="datastore" />""")
        )
        assertTrue(
            "data_extraction_rules must include progress_events.json",
            content.contains("""<include domain="file" path="progress_events.json" />""")
        )
        assertTrue(
            "data_extraction_rules must include sharedpref",
            content.contains("""<include domain="sharedpref" path="." />""")
        )
    }

    @Test
    fun fullBackupRules_excludesJournalAndIncludesProgressAndDataStore() {
        val rulesFile = File(projectRoot, "app/src/main/res/xml/backup_rules.xml")
        assertTrue("backup_rules.xml should exist", rulesFile.exists())
        val content = rulesFile.readText()

        // Excludes
        assertTrue(
            "backup_rules must exclude journal_entries.json",
            content.contains("""<exclude domain="file" path="journal_entries.json" />""")
        )
        assertTrue(
            "backup_rules must exclude journal_entries.json.bak",
            content.contains("""<exclude domain="file" path="journal_entries.json.bak" />""")
        )

        // Includes
        assertTrue(
            "backup_rules must include datastore",
            content.contains("""<include domain="file" path="datastore" />""")
        )
        assertTrue(
            "backup_rules must include progress_events.json",
            content.contains("""<include domain="file" path="progress_events.json" />""")
        )
        assertTrue(
            "backup_rules must include sharedpref",
            content.contains("""<include domain="sharedpref" path="." />""")
        )
    }
}
