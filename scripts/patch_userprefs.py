import shutil, os

# Patch UserPreferences.kt — add new profile keys after INSIGHT_DISMISSED_DATE
up_path = "C:/Users/Krishna/Nexus/core/datastore/src/main/kotlin/com/nexus/datastore/UserPreferences.kt"
with open(up_path, "r") as f:
    lines = f.readlines()

# Find the line with the final "}" of companion object
insert_idx = None
for i in range(len(lines) - 1, -1, -1):
    stripped = lines[i].strip()
    if stripped == "}" and "companion" not in lines[i]:
        insert_idx = i
        break

if insert_idx is None:
    print("ERROR: Could not find closing brace")
    exit(1)

new_block = """
  // NEXUS 2.0 profile fields
  val USER_COLLEGE = stringPreferencesKey("user_college")
  val USER_BRANCH = stringPreferencesKey("user_branch")
  val USER_YEAR = intPreferencesKey("user_year")
  val PLACEMENT_SEASON_DATE = longPreferencesKey("placement_season_date")
  val TARGET_COMPANY_TIERS = stringPreferencesKey("target_company_tiers")

  val userCollege: Flow<String> = context.dataStore.data.map { prefs -> prefs[USER_COLLEGE] ?: "" }
  val userBranch: Flow<String> = context.dataStore.data.map { prefs -> prefs[USER_BRANCH] ?: "" }
  val userYear: Flow<Int> = context.dataStore.data.map { prefs -> prefs[USER_YEAR] ?: 3 }
  val placementSeasonDate: Flow<Long> = context.dataStore.data.map { prefs -> prefs[PLACEMENT_SEASON_DATE] ?: 0L }
  val targetCompanyTiers: Flow<String> = context.dataStore.data.map { prefs -> prefs[TARGET_COMPANY_TIERS] ?: "TIER_1_PRODUCT,TIER_2_PRODUCT" }

  suspend fun setUserCollege(college: String) { context.dataStore.edit { prefs -> prefs[USER_COLLEGE] = college } }
  suspend fun setUserBranch(branch: String) { context.dataStore.edit { prefs -> prefs[USER_BRANCH] = branch } }
  suspend fun setUserYear(year: Int) { context.dataStore.edit { prefs -> prefs[USER_YEAR] = year } }
  suspend fun setPlacementSeasonDate(dateMillis: Long) { context.dataStore.edit { prefs -> prefs[PLACEMENT_SEASON_DATE] = dateMillis } }
  suspend fun setTargetCompanyTiers(tiers: String) { context.dataStore.edit { prefs -> prefs[TARGET_COMPANY_TIERS] = tiers } }
}
"""

lines.insert(insert_idx, new_block)
with open(up_path, "w") as f:
    f.writelines(lines)
print(
    f"UserPreferences.kt patched: inserted {len(new_block.splitlines())} lines before brace at line {insert_idx + 1}"
)
