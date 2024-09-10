# Protecting your services - Throttling is hard

# Problem
- What resource needs protection cause it is limited?

# Solutions
Other strategies? Caching

throttling is similar to a semaphore, but more just


Simple Case: user is authorized, then bucket throttle per user id



Hard Case: user is not authorized, how to make it just?
identify user by IP, what if it is a NAT/VPN?


IP + user agent

# Conclusion
