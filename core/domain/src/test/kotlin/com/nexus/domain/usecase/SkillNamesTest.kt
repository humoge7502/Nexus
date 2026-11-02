package com.nexus.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillNamesTest {

    @Test
    fun `spelling variants of one skill collapse`() {
        assertEquals(SkillNames.canonical("React"), SkillNames.canonical("ReactJS"))
        assertEquals(SkillNames.canonical("React"), SkillNames.canonical("React.js"))
        assertEquals(SkillNames.canonical("Node.js"), SkillNames.canonical("node"))
        assertEquals(SkillNames.canonical("Kubernetes"), SkillNames.canonical("K8s"))
    }

    @Test
    fun `abbreviations resolve to the spelled-out skill`() {
        assertEquals(SkillNames.canonical("Machine Learning"), SkillNames.canonical("ML"))
        assertEquals(SkillNames.canonical("Machine Learning"), SkillNames.canonical("Machine Learning (ML)"))
        assertEquals(SkillNames.canonical("Data Science"), SkillNames.canonical("DS"))
    }

    @Test
    fun `trailing qualifier word does not rename the skill`() {
        assertEquals(SkillNames.canonical("Android"), SkillNames.canonical("Android Development"))
        assertEquals(SkillNames.canonical("Python"), SkillNames.canonical("Python Programming"))
    }

    @Test
    fun `a qualifier that carries meaning is not stripped`() {
        assertFalse(SkillNames.same("Product Design", "Product"))
        assertFalse(SkillNames.same("Software Engineering", "Software"))
    }

    @Test
    fun `similar names that are different skills stay apart`() {
        assertFalse(SkillNames.same("JavaScript", "Java"))
        assertFalse(SkillNames.same("TypeScript", "JavaScript"))
        assertFalse(SkillNames.same("C++", "C"))
        assertFalse(SkillNames.same("PostgreSQL", "SQL"))
    }

    @Test
    fun `slash compounds fold to one skill`() {
        assertEquals(SkillNames.canonical("UI/UX"), SkillNames.canonical("UI-UX"))
        assertTrue(SkillNames.same("UI/UX Design", "UI/UX"))
    }

    @Test
    fun `blank names never match anything`() {
        assertEquals("", SkillNames.canonical("   "))
        assertEquals("", SkillNames.canonical("///"))
        assertFalse(SkillNames.same("", ""))
        assertFalse(SkillNames.same("React", ""))
    }

    @Test
    fun `canonical set drops blanks and deduplicates`() {
        assertEquals(setOf("react", "python"), SkillNames.canonicalSet(listOf("React.js", "Python", "React", " ")))
    }
}
