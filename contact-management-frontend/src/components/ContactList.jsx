import React from 'react'
import Contact from "./Contact"

const ContactList = ({data, currentPage, getAllContacts, deleteContact}) => {
  const isFirstPage = currentPage === 0;
  const isLastPage = data?.totalPages ? currentPage === data.totalPages - 1 : true;

  return (
    <section className='main'>
            {data?.content?.length === 0 && <div> No Contacts! Please Add a New Contact </div>}
            <ul className='contact__list'>
                {data?.content?.length > 0 && data.content.map(contact => <Contact contact={contact} key={contact.id} deleteContact={deleteContact} /> )}
            </ul>

            {data?.content?.length > 0 && data?.totalPages > 1 &&
                <div className='pagination'>
                    <button
                        type="button"
                        onClick={() => getAllContacts(currentPage - 1)}
                        disabled={isFirstPage}
                        className={isFirstPage ? 'disabled' : ''}
                    > &laquo; </button>

                    {[...new Array(data.totalPages).keys()].map((page) =>
                        <button
                            type="button"
                            onClick={() => getAllContacts(page)}
                            className={currentPage === page ? 'active' : ''}
                            key={page}
                        > {page + 1} </button>
                    )}

                    <button
                        type="button"
                        onClick={() => getAllContacts(currentPage + 1)}
                        disabled={isLastPage}
                        className={isLastPage ? 'disabled' : ''}
                    > &raquo; </button>
                </div>
            }
    </section>
  )
}

export default ContactList