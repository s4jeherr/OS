#include unistd.h
#include sysdep-cancel.h

 Read NBYTES into BUF from FD.  Return the number read or -1.  
ssize_t
__libc_read (int fd, void buf, size_t nbytes)
{
  return SYSCALL_CANCEL (read, fd, buf, nbytes);
}
libc_hidden_def (__libc_read)

libc_hidden_def (__read)
weak_alias (__libc_read, __read)
libc_hidden_def (read)
weak_alias (__libc_read, read)