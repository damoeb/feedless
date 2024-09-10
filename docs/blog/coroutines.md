# coroutines will safe you

# Problem
- blocking/sequential code execution is simple to understand but slow
- batch scraping websites is not resource efficient
- Execution timings become arbitrary

# Solution
- async execution
- But which one?
  - seqencial vs parallel
  - or sync vs async
  - single thread vs multi thread

- two options: flux or coroutines

# Requirements
- coroutines but with blocking database access
- no full rewrite (flux changes your syntax too much)

# Conclusion
- coroutines are super simple to implement
- C. improve performance a lot
- Mixing blocking and non-blocking works
- 
