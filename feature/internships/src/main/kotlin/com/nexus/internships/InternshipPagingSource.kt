package com.nexus.internships

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nexus.data.repository.InternshipRepository
import com.nexus.domain.model.Internship

class InternshipPagingSource(
    private val repository: InternshipRepository,
    private val remoteOnly: Boolean = false,
) : PagingSource<Int, Internship>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Internship> {
        return try {
            val page = params.key ?: 0
            val internships = if (!remoteOnly) {
                repository.getCachedInternshipsPage(page, params.loadSize)
            } else emptyList()

            if (internships.isNotEmpty() && page == 0) {
                LoadResult.Page(
                    data = internships,
                    prevKey = null,
                    nextKey = if (internships.size < params.loadSize) null else page + 1
                )
            } else {
                val result = repository.refreshInternshipsPage(page, params.loadSize)
                if (result.isSuccess) {
                    val data = result.dataOrNull ?: emptyList()
                    LoadResult.Page(
                        data = data,
                        prevKey = null,
                        nextKey = if (data.size < params.loadSize) null else page + 1
                    )
                } else {
                    LoadResult.Error(Exception(result.errorMessage ?: "Unknown error"))
                }
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Internship>): Int? = null
}
